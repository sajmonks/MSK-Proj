package wat.shop.abstracts;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import hla.rti1516e.CallbackModel;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.ResignAction;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.FederateServiceInvocationsAreBeingReportedViaMOM;
import hla.rti1516e.exceptions.FederatesCurrentlyJoined;
import hla.rti1516e.exceptions.FederationExecutionAlreadyExists;
import hla.rti1516e.exceptions.FederationExecutionDoesNotExist;
import hla.rti1516e.exceptions.InteractionClassNotDefined;
import hla.rti1516e.exceptions.InteractionClassNotPublished;
import hla.rti1516e.exceptions.InteractionParameterNotDefined;
import hla.rti1516e.exceptions.InvalidInteractionClassHandle;
import hla.rti1516e.exceptions.InvalidLogicalTime;
import hla.rti1516e.exceptions.NameNotFound;
import hla.rti1516e.exceptions.NotConnected;
import hla.rti1516e.exceptions.RTIexception;
import hla.rti1516e.exceptions.RTIinternalError;
import hla.rti1516e.exceptions.RestoreInProgress;
import hla.rti1516e.exceptions.SaveInProgress;
import hla.rti1516e.exceptions.TimeConstrainedAlreadyEnabled;
import hla.rti1516e.exceptions.TimeRegulationAlreadyEnabled;
import hla.rti1516e.time.HLAfloat64Interval;
import hla.rti1516e.time.HLAfloat64Time;
import hla.rti1516e.time.HLAfloat64TimeFactory;
import wat.shop.utils.Interaction;

public abstract class Federate {
	public static final int ITERATIONS = 20;

	public static final String READY_TO_RUN = "READY_TO_RUN";
	public RTIambassador rtiamb;
	
	
	private HLAfloat64TimeFactory timeFactory;
	
	
	protected EncoderFactory encoderFactory;
	
	protected Ambassador fedamb;
	
	protected String federateName;
	protected String federationName;
	
	protected boolean isRunning = true;
	
	protected abstract void onRun() throws RTIexception;
	protected abstract void publishAndSubscribe() throws RTIexception;
	protected abstract void handleInteraction(Interaction i) throws RTIexception;
	
	public void runFederate(String federateName, String federateType, String federation, double stepTime, Ambassador ambassador) throws Exception
	{
		
		this.federateName = federateName;
		this.federationName = federation;
		
		log("Tworzenie federata");
		rtiamb = RtiFactoryFactory.getRtiFactory().getRtiAmbassador();
		encoderFactory = RtiFactoryFactory.getRtiFactory().getEncoderFactory();
		
		log("Do³¹czanie ambasadora do RTI");
		fedamb = ambassador;
		rtiamb.connect(fedamb, CallbackModel.HLA_EVOKED);
		
		log("Tworzenie federacji");
		try {
			URL[] foms = new URL[] {
					(new File("fom/shop.fed").toURI().toURL())
			};
			rtiamb.createFederationExecution(federation, foms);
			log("Pomyœlnie utworzono federacje");
		}
		catch( FederationExecutionAlreadyExists exists ) {
			log("Federacja ju¿ istnieje....");
		}
		catch( MalformedURLException urle ) {
			log("B³ad ³adowania pliku fom: " + urle.getMessage());
			return;
		}
		
		log("Do³¹czanie do federacji");
		
		URL[] joinFoms = new URL[] {
				(new File("fom/shop.fed").toURI().toURL())
		};
		
		System.out.println(federateName + " " + federateType + " " + federation);
		rtiamb.joinFederationExecution(federateName, federateType, federation, joinFoms);
		log("Do³¹czono do federacji jako " + federateName);
		
		log("Ustawianie punktu synchronizacji.");
		this.timeFactory = (HLAfloat64TimeFactory)rtiamb.getTimeFactory();
		
		rtiamb.registerFederationSynchronizationPoint(READY_TO_RUN, null);
		while(fedamb.isAnnounced == false) { rtiamb.evokeMultipleCallbacks(0.1, 0.2); }
		
		waitForUser();
		
		rtiamb.synchronizationPointAchieved(READY_TO_RUN);
		log("Oczekiwanie na federacje....");
		while(fedamb.isReadyToRun == false) { rtiamb.evokeMultipleCallbacks(0.1, 0.2); }
		
		enableTimePolicy();
		publishAndSubscribe();
		
		log("Rozpoczynanie symulacji");
		while(isRunning) {
			advanceTime( stepTime );
			
			for(Interaction inter : fedamb.getInteractions()) {
				handleInteraction(inter);
			}
			fedamb.clearInteractions();
			
			onRun();
		}
		
		rtiamb.resignFederationExecution( ResignAction.DELETE_OBJECTS );
		try {
			rtiamb.destroyFederationExecution( federation );
		}
		catch( FederationExecutionDoesNotExist dne )
		{
			log( "Nie mo¿na zniszczyæ federacji bo nie istnieje." );
		}
		catch( FederatesCurrentlyJoined fcj )
		{
			log( "Nie mo¿na zniszczyæ federacji poniewa¿ dalej dzia³a." );
		}
	}
	
	public String getName() { 
		return federateName; 
	}
	
	public ParameterHandle getParameterHandle(InteractionClassHandle handle, String parameter) {
		try {
			return rtiamb.getParameterHandle(handle, parameter);
		} catch (NameNotFound e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidInteractionClassHandle e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FederateNotExecutionMember e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotConnected e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RTIinternalError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private void enableTimePolicy() throws Exception
	{
		HLAfloat64Interval lookahead = timeFactory.makeInterval( fedamb.federateLookahead );
		
		try {
			this.rtiamb.enableTimeRegulation( lookahead );
		} catch(TimeRegulationAlreadyEnabled e) {
			log("Time regulating jest ju¿ ustawiony");
		}
		

		while( fedamb.isRegulating == false )
		{
			rtiamb.evokeMultipleCallbacks( 0.1, 0.2 );
		}
	
		try {
			this.rtiamb.enableTimeConstrained();
		} catch(TimeConstrainedAlreadyEnabled e) {
			log("Time constrained jest ju¿ ustawiony");
		}
		
		while( fedamb.isConstrained == false )
		{
			rtiamb.evokeMultipleCallbacks( 0.1, 0.2 );
		}
	}
	
	private void advanceTime( double timestep ) throws RTIexception
	{
		log("Trying to advance from " + fedamb.federateTime + " to " + (fedamb.federateTime + timestep ) );
		fedamb.isAdvancing = true;
		HLAfloat64Time time = timeFactory.makeTime( fedamb.federateTime + timestep );
		rtiamb.timeAdvanceRequest( time );
		
		while( fedamb.isAdvancing )
		{
			rtiamb.evokeMultipleCallbacks( 0.1, 0.2 );
		}
	}
	
	protected void publishInteraction(String name) throws NameNotFound, FederateNotExecutionMember, NotConnected, RTIinternalError, InteractionClassNotDefined, SaveInProgress, RestoreInProgress {
		InteractionClassHandle servedHandle = rtiamb.getInteractionClassHandle("InteractionRoot."+name);
		rtiamb.publishInteractionClass( servedHandle );
	}
	
	protected void subscribeToInteraction(String name, String[] strings) throws NameNotFound, FederateNotExecutionMember, NotConnected, RTIinternalError, FederateServiceInvocationsAreBeingReportedViaMOM, InteractionClassNotDefined, SaveInProgress, RestoreInProgress {
		InteractionClassHandle servedHandle = rtiamb.getInteractionClassHandle("InteractionRoot."+name);
		rtiamb.subscribeInteractionClass( servedHandle );
		fedamb.registerHandle(name, servedHandle, strings);
		
	}
	
	protected void sendInteraction(String interaction, double step) throws FederateNotExecutionMember, NotConnected, InvalidLogicalTime, InteractionClassNotPublished, InteractionParameterNotDefined, InteractionClassNotDefined, SaveInProgress, RestoreInProgress, RTIinternalError, NameNotFound {
		ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(0);
		InteractionClassHandle servedHandle = rtiamb.getInteractionClassHandle("InteractionRoot."+interaction);
		HLAfloat64Time time = timeFactory.makeTime( fedamb.federateTime + fedamb.federateLookahead + step);
		rtiamb.sendInteraction(servedHandle, parameters, generateTag(), time);
	}
	
	protected void sendInteraction(String interaction,
			String n1, byte[] data1,
			double step
			) throws FederateNotExecutionMember, NotConnected, InvalidLogicalTime, InteractionClassNotPublished, InteractionParameterNotDefined, InteractionClassNotDefined, SaveInProgress, RestoreInProgress, RTIinternalError, NameNotFound, InvalidInteractionClassHandle 
	{
		ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(0);
		InteractionClassHandle servedHandle = rtiamb.getInteractionClassHandle("InteractionRoot."+interaction);
		
		ParameterHandle handle1 = rtiamb.getParameterHandle(servedHandle, n1);
		
		parameters.put(handle1, data1);
		
		HLAfloat64Time time = timeFactory.makeTime( fedamb.federateTime + fedamb.federateLookahead + step);
		rtiamb.sendInteraction(servedHandle, parameters, generateTag(), time);
	}
	
	protected void sendInteraction(String interaction,
			String n1, byte[] data1, 
			String n2, byte[] data2,
			double step
			) throws FederateNotExecutionMember, NotConnected, InvalidLogicalTime, InteractionClassNotPublished, InteractionParameterNotDefined, InteractionClassNotDefined, SaveInProgress, RestoreInProgress, RTIinternalError, NameNotFound, InvalidInteractionClassHandle 
	{
		ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(2);
		InteractionClassHandle servedHandle = rtiamb.getInteractionClassHandle("InteractionRoot."+interaction);
		
		ParameterHandle handle1 = rtiamb.getParameterHandle(servedHandle, n1);
		ParameterHandle handle2 = rtiamb.getParameterHandle(servedHandle, n2);
		
		parameters.put(handle1, data1);
		parameters.put(handle2, data2);
		
		HLAfloat64Time time = timeFactory.makeTime( fedamb.federateTime + fedamb.federateLookahead + step);
		rtiamb.sendInteraction(servedHandle, parameters, generateTag(), time);
	}
	
	protected void sendInteraction(String interaction,
			String n1, byte[] data1, 
			String n2, byte[] data2, 
			String n3, byte[] data3,
			double step
			) throws FederateNotExecutionMember, NotConnected, InvalidLogicalTime, InteractionClassNotPublished, InteractionParameterNotDefined, InteractionClassNotDefined, SaveInProgress, RestoreInProgress, RTIinternalError, NameNotFound, InvalidInteractionClassHandle 
	{
		ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(3);
		InteractionClassHandle servedHandle = rtiamb.getInteractionClassHandle("InteractionRoot."+interaction);
		
		ParameterHandle handle1 = rtiamb.getParameterHandle(servedHandle, n1);
		ParameterHandle handle2 = rtiamb.getParameterHandle(servedHandle, n2);
		ParameterHandle handle3 = rtiamb.getParameterHandle(servedHandle, n3);
		
		parameters.put(handle1, data1);
		parameters.put(handle2, data2);
		parameters.put(handle3, data3);
		
		HLAfloat64Time time = timeFactory.makeTime( fedamb.federateTime + fedamb.federateLookahead + step);
		rtiamb.sendInteraction(servedHandle, parameters, generateTag(), time);
	}
	
	protected byte[] generateTag()
	{
		return ("(timestamp) "+System.currentTimeMillis()).getBytes();
	}
	
	protected void waitForUser()
	{
		BufferedReader reader = new BufferedReader( new InputStreamReader(System.in) ); try { reader.readLine(); }catch( Exception e ) {}
	}
	
	protected void log( String message ) { System.out.println( federateName + "  : " + message ); }
		
}

package wat.shop.abstracts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import hla.rti1516e.FederateHandleSet;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.NullFederateAmbassador;
import hla.rti1516e.OrderType;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.SynchronizationPointFailureReason;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.exceptions.FederateInternalError;
import hla.rti1516e.time.HLAfloat64Time;
import wat.shop.utils.Encoder;
import wat.shop.utils.Interaction;

public abstract class Ambassador extends NullFederateAmbassador {
	
	private Federate fed;
	private ArrayList<Interaction> interactionList = new ArrayList<Interaction>();
	private HashMap<String, InteractionClassHandle> interactionHandles = new HashMap<String, InteractionClassHandle>();
	private HashMap<InteractionClassHandle, ArrayList<String>> interactionParameters = new HashMap<InteractionClassHandle, ArrayList<String>>();
	
	public double federateLookahead;
	public double federateTime;
	
	public boolean isAnnounced;
	public boolean isReadyToRun;
	public boolean isRegulating;
	public boolean isConstrained;
	public boolean isAdvancing;


	public Ambassador(Federate federate, double lookahead) {
		this.fed = federate;
		this.federateLookahead = lookahead;
	}
	
	@Override
	public void synchronizationPointRegistrationFailed( String label,
	                                                    SynchronizationPointFailureReason reason )
	{
		log( "B³¹d podczas rejestracji punktu synchronizacji " + label + ", powód="+reason );
	}

	@Override
	public void synchronizationPointRegistrationSucceeded( String label )
	{
		log( "Uda³o siê osi¹gn¹æ sychronizacje czasow¹: " + label );
	}

	@Override
	public void announceSynchronizationPoint( String label, byte[] tag )
	{
		log( "Og³oszono punkt sychronizacji: " + label );
		if( label.equals(Federate.READY_TO_RUN) )
			this.isAnnounced = true;
	}

	@Override
	public void federationSynchronized( String label, FederateHandleSet failed )
	{
		log( "Federacje zsynchronizowana: " + label );
		if( label.equals(Federate.READY_TO_RUN) )
			this.isReadyToRun = true;
	}

	/**
	 * The RTI has informed us that time regulation is now enabled.
	 */
	@Override
	public void timeRegulationEnabled( LogicalTime time )
	{
		this.federateTime = ((HLAfloat64Time)time).getValue();
		this.isRegulating = true;
	}

	@Override
	public void timeConstrainedEnabled( LogicalTime time )
	{
		this.federateTime = ((HLAfloat64Time)time).getValue();
		this.isConstrained = true;
	}

	@Override
	public void timeAdvanceGrant( LogicalTime time )
	{
		this.federateTime = ((HLAfloat64Time)time).getValue();
		this.isAdvancing = false;
	}
	
	@Override
	public void receiveInteraction( InteractionClassHandle interactionClass,
	                                ParameterHandleValueMap theParameters,
	                                byte[] tag,
	                                OrderType sentOrdering,
	                                TransportationTypeHandle theTransport,
	                                LogicalTime time,
	                                OrderType receivedOrdering,
	                                SupplementalReceiveInfo receiveInfo )
	    throws FederateInternalError
	{
		ArrayList<String> debugParams = new ArrayList<String>();
		ArrayList<byte[] > debugValues = new ArrayList<byte[] >();
		
		for(String key : interactionHandles.keySet()) {
			InteractionClassHandle handle = interactionHandles.get(key);
			if(handle.equals(interactionClass)) {
				Interaction interaction = new Interaction(key);
				interaction.setTime( ((HLAfloat64Time) time).getValue() );
				
				if(interactionParameters.containsKey(handle)) {
					ArrayList <String> params = interactionParameters.get(handle);
					for(String param : params) {
						
						ParameterHandle paramhandle = fed.getParameterHandle(handle, param);
						
						if(paramhandle == null)
							continue;
						
						for(ParameterHandle sentParam : theParameters.keySet()) {
							if(sentParam.equals(paramhandle)) {
								debugParams.add(param);
								debugValues.add(theParameters.get(sentParam));
								
								interaction.addParameter(param, theParameters.get(sentParam));
							}
						}		
					}
				}
				
				//Debug
				String debugMessage = "Odebrano " + interaction.getName() + " ";
				for(int i = 0; i < debugParams.size(); i++) {
					debugMessage += debugParams.get(i) + "=" +
							Encoder.decodeInt(fed.encoderFactory, debugValues.get(i)) + " ";
				}
				log(debugMessage);
				
				interactionList.add(interaction);
			}
		}
	}
	
	public void registerHandle(String name, InteractionClassHandle handle, String [] params) {
		if(!interactionHandles.containsKey(name)) {
			interactionHandles.put(name, handle);
		}
		
		if(!interactionParameters.containsKey(handle) && params != null) {
				interactionParameters.put(handle, new ArrayList<>(Arrays.asList(params)));
		}
	}
	
	public double getSimulationTime() {
		return this.federateTime;
	}
	
	public void sortInteractions() {
		Collections.sort(interactionList, new Interaction.TimeComparator());
	}
	
	public ArrayList<Interaction> getInteractions() {
		return interactionList;
	}
	
	public void clearInteractions() {
		interactionList.clear();
	}
	
	private void log( String message ) { System.out.println( fed.getName() + "Ambassador  : " + message ); }
}

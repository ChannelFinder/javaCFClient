package gov.bnl.channelfinder.channelfinderAPI;

import gov.bnl.channelfinder.channelfinderAPI.exceptions.ChannelFinderException;
import gov.bnl.channelfinder.model.XmlChannel;

import org.junit.Test;

public class ErrorConditionTest {

	@Test
	public void error(){
		
	}
	
	
	
	/**
	 * Test detection of error condition - attempting to update a channel that
	 * does not exist
	 */
	@Test
	public void updateEmptyChannel() {
		try {
			XmlChannel channel = new XmlChannel("new", "owner");
			ChannelFinderClient.getInstance().updateChannel(channel);
		} catch (ChannelFinderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

package edu.arizona.biosemantics.oto2.steps.shared.log;

import edu.arizona.biosemantics.common.log.AbstractLogInjection;
import edu.arizona.biosemantics.common.log.ILoggable;

public aspect LogInjection extends AbstractLogInjection {
	
	declare parents : edu.arizona.biosemantics.oto2.steps.server..* implements ILoggable;
}

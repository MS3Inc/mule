/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.quartz.jobs;

import org.mule.MuleManager;
import org.mule.config.i18n.Message;
import org.mule.impl.MuleMessage;
import org.mule.providers.AbstractConnector;
import org.mule.providers.AbstractMessageReceiver;
import org.mule.providers.NullPayload;
import org.mule.providers.quartz.QuartzConnector;
import org.mule.providers.quartz.QuartzMessageReceiver;
import org.mule.registry.RegistryException;
import org.mule.umo.manager.ObjectNotFoundException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Invokes a Quartz Message receiver with the payload attached to the Quartz job.
 */
public class MuleReceiverJob implements Job
{

    /**
     * The logger used for this class
     */
    protected transient Log logger = LogFactory.getLog(getClass());

    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException
    {
        JobDataMap map = jobExecutionContext.getJobDetail().getJobDataMap();

        String receiverKey = (String)map.get(QuartzMessageReceiver.QUARTZ_RECEIVER_PROPERTY);
        if (receiverKey == null)
            throw new JobExecutionException(new Message("quartz", 5).getMessage());

        String connectorName = (String)map.get(QuartzMessageReceiver.QUARTZ_CONNECTOR_PROPERTY);
        if (connectorName == null)
            throw new JobExecutionException(new Message("quartz", 6).getMessage());

        AbstractConnector connector = null;
        try
        {
            connector = (AbstractConnector)MuleManager.getRegistry().lookupConnector(connectorName);
        }
        catch (RegistryException e)
        {
            logger.info("Unable to look up connector " + connectorName + " : " + e.getMessage());
        }
        if (connector == null)
            throw new JobExecutionException(new Message("quartz", 7, connectorName).getMessage());

        AbstractMessageReceiver receiver = (AbstractMessageReceiver)connector.lookupReceiver(receiverKey);
        if (receiver == null)
            throw new JobExecutionException(new Message("quartz", 8, connectorName, receiverKey).getMessage());

        Object payload = jobExecutionContext.getJobDetail().getJobDataMap().get(
            QuartzConnector.PROPERTY_PAYLOAD);

        try
        {
            if (payload == null)
            {
                String ref = jobExecutionContext.getJobDetail().getJobDataMap().getString(
                    QuartzConnector.PROPERTY_PAYLOAD_REFERENCE);
                // for backward compatibility check the old payload Class property
                // too
                if (ref == null)
                {
                    ref = jobExecutionContext.getJobDetail().getJobDataMap().getString(
                        QuartzConnector.PROPERTY_PAYLOAD_CLASS_NAME);
                }
                try
                {
                    if (ref == null)
                    {
                        payload = new NullPayload();
                    }
                    else 
                    {
                        payload = MuleManager.getInstance().getContainerContext().getComponent(ref);
                    }
                }
                catch (ObjectNotFoundException e)
                {
                    logger.warn("There is no payload attached to this quartz job. Sending Null payload");
                    payload = new NullPayload();
                }
            }
            receiver.routeMessage(new MuleMessage(receiver.getConnector().getMessageAdapter(payload)));
        }
        catch (Exception e)
        {
            receiver.handleException(e);
        }
    }
}

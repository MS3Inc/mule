/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.api.context.notification;

/**
 * Observer interface to receive notifications about messages being sent and received from connectors
 */
public interface ConnectorMessageNotificationListener<T extends ConnectorMessageNotification>
    extends ServerNotificationListener<ConnectorMessageNotification> {
  // no methods
}
/**
 * 
 */
package com.microsoft.windowsazure.log4j;

import java.sql.Timestamp;
import java.util.Date;

import com.microsoft.windowsazure.services.table.client.TableServiceEntity;

/**
 * @author jurgenma
 *
 */
public class LogEntity extends TableServiceEntity {

	private String message;
	
	private String level;
	
	private String deploymentId;
	
	private String roleInstanceId;
	
	public LogEntity(final String partKey, final String rowKey) {
		
		super();
		this.partitionKey = partKey;
		this.rowKey = rowKey;
		this.timeStamp = new Date();
	}
	
    /**
     * @param partKey
     * @param rowKey
     * @param message
     * @param level
     */
    public LogEntity(final String partKey, final String rowKey, final String message, final String level) {
		this(partKey, rowKey);
		this.message = message;
		this.level = level;
	}

	/**
	 * @return the message
	 */
	public final String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public final void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the level
	 */
	public final String getLevel() {
		return level;
	}

	/**
	 * @param level the level to set
	 */
	public final void setLevel(String level) {
		this.level = level;
	}

	/**
	 * @return
	 */
	public final Date getLogTime() {
    	Timestamp reversedTimestamp = new Timestamp(Long.valueOf(getRowKey())); 
    	Timestamp maxTimestamp = new Timestamp(new Date(Long.MAX_VALUE).getTime());
    	return new Date(maxTimestamp.getTime() - reversedTimestamp.getTime());
    }

	/**
	 * @return the deploymentId
	 */
	public final String getDeploymentId() {
		return deploymentId;
	}

	/**
	 * @param deploymentId the deploymentId to set
	 */
	public final void setDeploymentId(String deploymentId) {
		this.deploymentId = deploymentId;
	}

	/**
	 * @return the roleInstanceId
	 */
	public final String getRoleInstanceId() {
		return roleInstanceId;
	}

	/**
	 * @param roleInstanceId the roleInstanceId to set
	 */
	public final void setRoleInstanceId(String roleInstanceId) {
		this.roleInstanceId = roleInstanceId;
	}

}

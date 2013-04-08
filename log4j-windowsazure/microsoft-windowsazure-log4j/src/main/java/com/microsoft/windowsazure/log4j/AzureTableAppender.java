package com.microsoft.windowsazure.log4j;

import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LoggingEvent;

import com.microsoft.windowsazure.serviceruntime.RoleEnvironment;
import com.microsoft.windowsazure.services.core.storage.CloudStorageAccount;
import com.microsoft.windowsazure.services.core.storage.StorageCredentialsAccountAndKey;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.table.client.CloudTable;
import com.microsoft.windowsazure.services.table.client.TableOperation;

/**
 * @author jurgenma
 * 
 */
public class AzureTableAppender extends AppenderSkeleton {

	private String tableStorageEndpoint;
	private String accountName;
	private String accountKey;
	private String tableName = "Logs";
	private boolean createTableIfNotExists = true;
	private boolean useHttps = false;

	private CloudTable cloudTable;

	@Override
	public void close() {
	}

	@Override
	public boolean requiresLayout() {
		return true;
	}

	@Override
	protected void append(final LoggingEvent loggingEvent) {

		if (this.cloudTable == null) {
			try {
				CloudStorageAccount account = this.createCloudStorageAccount();
				this.cloudTable = account.createCloudTableClient()
						.getTableReference(this.tableName);
				if (this.createTableIfNotExists)
					this.cloudTable.createIfNotExist();

			} catch (URISyntaxException ex) {
				throw new IllegalStateException(
						"Wrong Azure storage account configuration", ex);
			} catch (StorageException e) {
				this.errorHandler.error("Error on trying to get Azure table reference", e, ErrorCode.FILE_OPEN_FAILURE);
			}
		}

		// Temporarily switch off logging to avoid infinite recursion
		// (The underlying Azure library also uses log4j !)
		Priority priority = this.getThreshold();
		this.setThreshold(Level.OFF);

		String message = "";
		try {
			message = this.layout.format(loggingEvent);

			// Construct row key based on the reversed timestamp
			Calendar calendar = Calendar.getInstance();
			Timestamp max = new Timestamp(new Date(Long.MAX_VALUE).getTime());
			String rowKey = String.valueOf(max.getTime()
					- calendar.getTimeInMillis());

			// Change the calendar to today's date to compute partition key
			calendar.set(Calendar.HOUR, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			String partitionKey = String.valueOf(max.getTime()
					- calendar.getTimeInMillis());

			String level = loggingEvent.getLevel().toString();

			this.cloudTable.getServiceClient().execute(
					this.tableName,
					TableOperation.insert(this.createLogEntity(partitionKey, rowKey, message, level)));

		} catch (StorageException storageEx) {
			this.errorHandler.error("Can not insert log message '" + message
					+ "' into Azure table '" + this.tableName
					+ "' of account '" + this.accountName + "'", storageEx,
					ErrorCode.WRITE_FAILURE);
			storageEx.printStackTrace();
		} finally {
			// Switch logging back on
			this.setThreshold(priority);
		}
	}

	/**
	 * @return
	 * @throws URISyntaxException
	 */
	private CloudStorageAccount createCloudStorageAccount()
			throws URISyntaxException {

		CloudStorageAccount account = null;

		if (this.isUsingDevStorageAccount()) {
			account = CloudStorageAccount.getDevelopmentStorageAccount();
		} else {
			if (this.accountName == null || this.accountName.length() == 0)
				throw new IllegalStateException(
						"Windows Azure Storage account name must not be empty");

			if (this.accountKey == null || this.accountKey.length() == 0)
				throw new IllegalStateException(
						"Windows Azure Storage account key must not be empty");

			account = new CloudStorageAccount(
					new StorageCredentialsAccountAndKey(this.accountName,
							this.accountKey), this.useHttps);
		}

		return account;
	}
	
	/**
	 * @param partitionKey
	 * @param rowKey
	 * @param message
	 * @param level
	 * @return
	 */
	private LogEntity createLogEntity(final String partitionKey, final String rowKey, final String message, final String level) {
		
		LogEntity result = new LogEntity(partitionKey, rowKey,
				message, level);
		
		if (RoleEnvironment.isAvailable()) {
			result.setDeploymentId(RoleEnvironment.getDeploymentId());
			result.setRoleInstanceId(RoleEnvironment.getCurrentRoleInstance().getId());
		}
		
		return result;
	}

	/**
	 * @return
	 */
	private boolean isUsingDevStorageAccount() {

		boolean result = false;

		if (this.accountName != null
				&& this.accountName.startsWith("devstoreaccount")) {
			result = true;
		}

		return result;
	}

	/**
	 * @return the tableStorageEndpoint
	 */
	public final String getTableStorageEndpoint() {
		return tableStorageEndpoint;
	}

	/**
	 * @param tableStorageEndpoint
	 *            the tableStorageEndpoint to set
	 */
	public final void setTableStorageEndpoint(String tableStorageEndpoint) {
		this.tableStorageEndpoint = tableStorageEndpoint;
	}

	/**
	 * @return the accountName
	 */
	public final String getAccountName() {
		return accountName;
	}

	/**
	 * @param accountName
	 *            the accountName to set
	 */
	public final void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	/**
	 * @return the accountKey
	 */
	public final String getAccountKey() {
		return accountKey;
	}

	/**
	 * @param accountKey
	 *            the accountKey to set
	 */
	public final void setAccountKey(String accountKey) {
		this.accountKey = accountKey;
	}

	/**
	 * @return the tableName
	 */
	public final String getTableName() {
		return tableName;
	}

	/**
	 * @param tableName
	 *            the tableName to set
	 */
	public final void setTableName(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * @return the createTableIfNotExists
	 */
	public final boolean isCreateTableIfNotExists() {
		return createTableIfNotExists;
	}

	/**
	 * @param createTableIfNotExists
	 *            the createTableIfNotExists to set
	 */
	public final void setCreateTableIfNotExists(boolean createTableIfNotExists) {
		this.createTableIfNotExists = createTableIfNotExists;
	}

	/**
	 * @return the useHttps
	 */
	public final boolean isUseHttps() {
		return useHttps;
	}

	/**
	 * @param useHttps
	 *            the useHttps to set
	 */
	public final void setUseHttps(boolean useHttps) {
		this.useHttps = useHttps;
	}
}

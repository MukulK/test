import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.CloudStorageAccount;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class SftpToAzureBlobStorage {

    public static void main(String[] args) {
        String sftpHost = "your-sftp-host";
        int sftpPort = 22;
        String sftpUser = "your-sftp-username";
        String sftpPassword = "your-sftp-password";
        String sftpRemoteFilePath = "/path/to/your/file.txt";

        String azureStorageConnectionString = "your-azure-storage-connection-string";
        String azureBlobContainerName = "your-blob-container-name";
        String azureBlobName = "file.txt";

        try {
            // SFTP Connection
            JSch jsch = new JSch();
            Session session = jsch.getSession(sftpUser, sftpHost, sftpPort);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(sftpPassword);
            session.connect();

            // SFTP Channel
            ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            // Download file from SFTP to a local temporary file
            String localTempFilePath = "/path/to/local/temp/file.txt";
            channelSftp.get(sftpRemoteFilePath, localTempFilePath);

            // Azure Blob Storage Connection
            CloudStorageAccount storageAccount = CloudStorageAccount.parse(azureStorageConnectionString);
            CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
            CloudBlobContainer container = blobClient.getContainerReference(azureBlobContainerName);
            CloudBlockBlob blob = container.getBlockBlobReference(azureBlobName);

            // Upload local file to Azure Blob Storage
            try (InputStream inputStream = new FileInputStream(new File(localTempFilePath))) {
                blob.upload(inputStream, -1);
            }

            // Close connections
            channelSftp.disconnect();
            session.disconnect();

            // Cleanup local temporary file if needed
            // new File(localTempFilePath).delete();

            System.out.println("File transfer from SFTP to Azure Blob Storage completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

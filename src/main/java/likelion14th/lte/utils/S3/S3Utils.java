package likelion14th.lte.utils.S3;

import likelion14th.lte.global.config.AmazonConfig;
import likelion14th.lte.utils.exception.UtilException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import static likelion14th.lte.utils.exception.UtilException.Reason.*;

import java.io.InputStream;
import java.util.UUID;

@Component
@RequiredArgsConstructor (access = AccessLevel.PROTECTED)
@Slf4j
public class S3Utils {
    private final S3Client s3Client;
    private final AmazonConfig config;

    private String generateObjectKey(String fileName){
        return config.getLocationPath() + UUID.randomUUID() + "-" + fileName;
    }
    private String createFileUrl(String objectKey){
        return "https://" + config.getBucket()+".s3." + config.getRegion()+".amazon.com/"+objectKey;
    }

    public S3Dto uploadFile(MultipartFile file){
        if(file == null || file.isEmpty() || file.getSize() <= 0) {
            throw new UtilException(FILE_EMPTY);
        }

        String originalFilename = file.getOriginalFilename();
        if(originalFilename == null){
            throw new UtilException(FILE_EMPTY);
        }

        String objectKey = generateObjectKey(originalFilename);
        String contentType = (file.getContentType() != null)
                ? file.getContentType() : "application/octet-stream";

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(config.getBucket())
                .key(objectKey)
                .contentType(contentType)
                .build();

        try(InputStream inputStream = file.getInputStream()) {
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, file.getSize()));

            return new S3Dto(createFileUrl(objectKey), objectKey);
        }catch (SdkClientException e){
            throw new UtilException(S3_UPLOAD_FAILED, e);
        }catch (Exception e){
            throw new UtilException(S3_UPLOAD_FAILED, e);
        }
    }
    public S3Dto uploadBytes(byte[] bytes, String fileName, String contentType){
        if(bytes == null || bytes.length == 0){
            throw new UtilException(FILE_EMPTY);
        }
        if(fileName == null || fileName.isEmpty()){
            throw new UtilException(FILE_EMPTY);
        }
        String objectKey = generateObjectKey(fileName);
        String resolvedContentType = (contentType != null && !contentType.isBlank()
                ? contentType : "application/octet-stream");
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(config.getBucket())
                .key(objectKey)
                .contentType(resolvedContentType)
                .build();

        try{
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes));
            return new S3Dto(createFileUrl(objectKey), objectKey);
        } catch (SdkClientException e){
            throw new UtilException(S3_UPLOAD_FAILED, e);
        } catch (Exception e){
            throw new UtilException(S3_UPLOAD_FAILED, e);
        }
    }
    public void deleteFile(String objectKey){
        if(objectKey == null || objectKey.isBlank()){
            throw new UtilException(S3_DELETE_FAILED);
        }
        try{
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(config.getBucket())
                    .key(objectKey)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
            log.info("Deleted file {}", objectKey);
        } catch (SdkClientException e){
            log.error("Failed to delete S3 object: {}", objectKey, e);
            throw new UtilException(S3_DELETE_FAILED, e);
        } catch (Exception e){
            throw new UtilException(S3_DELETE_FAILED, e);
        }
    }
}

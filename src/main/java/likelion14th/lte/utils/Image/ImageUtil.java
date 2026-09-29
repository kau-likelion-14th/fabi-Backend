package likelion14th.lte.utils.Image;

import likelion14th.lte.utils.exception.UtilException;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Set;

import static likelion14th.lte.utils.exception.UtilException.Reason.*;

@Slf4j
@Component
public class ImageUtil {

    private static final Tika TIKA = new Tika();

    public record ResizedImage(byte[] bytes, String contentType, String extension) {}

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/png",
            "image/jpeg",
            "image/webp"
    );

    private static final long MAX_FILE_SIZE = 50L * 1024 * 1024; // 50MB

    public void validateImage(MultipartFile file) {

        if (file == null || file.isEmpty() || file.getSize() <= 0) {
            throw new UtilException(FILE_EMPTY);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new UtilException(FILE_TOO_LARGE);
        }

        try (InputStream is = file.getInputStream()) {
            String detectedContentType = TIKA.detect(is);
            if (!ALLOWED_CONTENT_TYPES.contains(detectedContentType)) {
                throw new UtilException(TYPE_NOT_ALLOWED);
            }
        } catch (UtilException e) {
            throw e; // 그대로 전파
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new UtilException(IMAGE_PROCESS_FAILED, e);
        }
    }

    public ResizedImage resizeProfileToPngBytes(MultipartFile file, int targetSize) {

        try {
            BufferedImage resizedImage;
            try (InputStream inputStream = file.getInputStream()) {
                resizedImage = Thumbnails.of(inputStream)
                        .size(targetSize, targetSize)
                        .crop(Positions.CENTER)
                        .asBufferedImage();
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            boolean writeSucceeded = ImageIO.write(resizedImage, "png", outputStream);

            if (!writeSucceeded) {
                throw new UtilException(IMAGE_PROCESS_FAILED);
            }

            return new ResizedImage(
                    outputStream.toByteArray(),
                    "image/png",
                    "png"
            );

        } catch (UtilException e) {
            throw e;
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new UtilException(IMAGE_PROCESS_FAILED, e);
        }
    }
}

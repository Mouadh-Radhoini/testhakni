package com.testhakni.backend.media.infrastructure;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.testhakni.backend.media.application.port.ImageStorage;
import com.testhakni.backend.media.domain.ImageFormat;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Component;

/**
 * Adaptateur GridFS du port {@link ImageStorage} : les images vivent dans la meme base MongoDB
 * (collections fs.files / fs.chunks), donc sauvegardees et deployees avec elle.
 */
@Component
class GridFsImageStorage implements ImageStorage {

    private final GridFsTemplate gridFs;

    GridFsImageStorage(GridFsTemplate gridFs) {
        this.gridFs = gridFs;
    }

    @Override
    public String store(byte[] content, ImageFormat format, String ownerId) {
        Document metadata = new Document("ownerId", ownerId).append("format", format.name());
        ObjectId id = gridFs.store(new ByteArrayInputStream(content), format.name().toLowerCase(),
                format.mediaType(), metadata);
        return id.toHexString();
    }

    @Override
    public Optional<StoredImage> load(String id) {
        if (!ObjectId.isValid(id)) return Optional.empty();
        GridFSFile file = gridFs.findOne(byId(id));
        if (file == null) return Optional.empty();
        try (var in = gridFs.getResource(file).getInputStream()) {
            String mediaType = file.getMetadata() == null ? null : file.getMetadata().getString("_contentType");
            return Optional.of(new StoredImage(in.readAllBytes(),
                    mediaType != null ? mediaType : "application/octet-stream"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void delete(String id) {
        if (ObjectId.isValid(id)) gridFs.delete(byId(id));
    }

    private static Query byId(String id) {
        return Query.query(Criteria.where("_id").is(new ObjectId(id)));
    }
}

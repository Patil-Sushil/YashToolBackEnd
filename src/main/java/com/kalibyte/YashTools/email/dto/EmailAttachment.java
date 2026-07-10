package com.kalibyte.YashTools.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailAttachment {
    private String filename;
    private String contentType;
    private byte[] data;
    private InputStream inputStream;
    private String description;
    private boolean inline;

    public InputStream openStream() {
        return inputStream != null ? inputStream : new ByteArrayInputStream(data);
    }

    public long sizeBytes() {
        return data != null ? data.length : -1;
    }
}
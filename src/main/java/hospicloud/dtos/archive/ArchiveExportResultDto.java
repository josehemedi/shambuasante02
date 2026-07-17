package hospicloud.dtos.archive;

public class ArchiveExportResultDto {
    private byte[] content;
    private String filename;
    private String contentType;
    private ArchiveExportFormat format;
    private long sizeBytes;
    private int pageCount;

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public ArchiveExportFormat getFormat() {
        return format;
    }

    public void setFormat(ArchiveExportFormat format) {
        this.format = format;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }
}

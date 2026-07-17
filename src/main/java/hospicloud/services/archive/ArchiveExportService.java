package hospicloud.services.archive;

import hospicloud.dtos.archive.ArchiveExportFormat;
import hospicloud.dtos.archive.ArchiveExportResultDto;

public interface ArchiveExportService {

    ArchiveExportResultDto exporter(Long archiveId, ArchiveExportFormat format);
}

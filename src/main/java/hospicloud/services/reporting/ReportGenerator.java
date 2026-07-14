package hospicloud.services.reporting;

import net.sf.jasperreports.engine.JRDataSource;
import java.util.Map;

public interface ReportGenerator {

    byte[] generate(String reportName,
                    Map<String, Object> params,
                    JRDataSource dataSource) throws Exception;
}
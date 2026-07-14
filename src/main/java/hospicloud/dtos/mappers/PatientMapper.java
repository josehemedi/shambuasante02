package hospicloud.dtos.mappers;

import hospicloud.dtos.PatientDto;
import hospicloud.model.Patient;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
public interface PatientMapper {
    PatientDto toDto(Patient entity);
    Patient toEntity(PatientDto dto);
}
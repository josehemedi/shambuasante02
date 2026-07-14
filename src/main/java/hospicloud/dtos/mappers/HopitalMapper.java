package hospicloud.dtos.mappers;

import hospicloud.dtos.HopitalDto;
import hospicloud.model.Hopital;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
public interface HopitalMapper {
    HopitalDto toDto(Hopital entity);
    Hopital toEntity(HopitalDto dto);
}
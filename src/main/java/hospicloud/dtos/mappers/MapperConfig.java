package hospicloud.dtos.mappers;


import org.mapstruct.ReportingPolicy;

@org.mapstruct.MapperConfig(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
interface MapStructConfig {
}
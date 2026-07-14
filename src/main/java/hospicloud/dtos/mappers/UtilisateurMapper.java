package hospicloud.dtos.mappers;

import hospicloud.dtos.UtilisateurDto;
import hospicloud.model.Utilisateur;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
public interface UtilisateurMapper {
    UtilisateurDto toDto(Utilisateur entity);
    Utilisateur toEntity(UtilisateurDto dto);
}
package org.esfe.servicios.implementaciones;

import org.esfe.modelos.User;
import org.esfe.repositorios.IUserRepository;
import org.esfe.servicios.interfaces.IUserService;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class UserServiceImpl implements IUserService {
    private final IUserRepository userRepository;
    // Inyección de dependencias a través del constructor
    public UserServiceImpl(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User guardar(User user) {
        // Buscar si ya existe un usuario con el mismo correo
        Optional<User> existenteOpt = userRepository.findByEmail(user.getEmail());

        if (existenteOpt.isPresent()) {
            // Sí existe, actualizar sus datos
            User existente = existenteOpt.get();

            // Actualizar campos que pueden venir desde el formulario
            existente.setStudentId(user.getStudentId());
            existente.setCareer(user.getCareer());
            existente.setNationalId(user.getNationalId());

            // Nombre puede venir del OAuth
            if (user.getFullName() != null) {
                existente.setFullName(user.getFullName());
            }
            // Guardar cambios en la base de datos
            return userRepository.save(existente);
        }

        // si no existe en la base de datos guardar como Nuevo usuario
        return userRepository.save(user);
    }

    @Override
    public Optional<User> buscarPorEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @SuppressWarnings("null")
    public Optional<User> buscarPorId(Integer id){
        return userRepository.findById(id);
    }
}

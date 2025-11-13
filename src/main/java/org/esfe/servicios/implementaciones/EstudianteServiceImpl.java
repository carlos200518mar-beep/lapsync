package org.esfe.servicios.implementaciones;

import org.esfe.modelos.User;
import org.esfe.repositorios.IEstudianteRepository;
import org.esfe.servicios.interfaces.IEstudianteService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
    public class EstudianteServiceImpl implements IEstudianteService {

    private final IEstudianteRepository estudianteRepository;

    public EstudianteServiceImpl(IEstudianteRepository estudianteRepository) {
        this.estudianteRepository = estudianteRepository;
    }

    @Override
    public Optional<User> buscarPorEmail(String email) {
        return estudianteRepository.findByEmail(email);
    }
}


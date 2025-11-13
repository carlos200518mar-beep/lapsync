package org.esfe.servicios.interfaces;

import org.esfe.modelos.Loans;
import org.esfe.modelos.User;

import java.util.List;

public interface ILoansService {
    Loans save(Loans loan);
    List<Loans> listarTodos();
    Loans buscarPorId(Integer id);
    List<Loans> buscarPorUsuario(User user);

    List<Loans> listarTodosOrdenadosPorFechaDesc();
    List<Loans> buscarPorUsuarioOrdenados(User user);
}

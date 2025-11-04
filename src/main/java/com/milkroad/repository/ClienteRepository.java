package com.milkroad.repository;

import com.milkroad.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    boolean existsByCelular(String celular);

    Optional<Cliente> findByNomeIgnoreCase(String nome);

    List<Cliente> findByAtivo(boolean ativo);

    Optional<Cliente> findByEmail(String email);

    List<Cliente> findByPerfilNot(String perfil);
}
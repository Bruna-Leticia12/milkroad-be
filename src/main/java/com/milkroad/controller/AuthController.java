package com.milkroad.controller;

import com.milkroad.dto.LoginRequest;
import com.milkroad.dto.LoginResponse;
import com.milkroad.entity.Cliente;
import com.milkroad.entity.Perfil;
import com.milkroad.repository.ClienteRepository;
import com.milkroad.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 🔑 LOGIN
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getSenha())
            );

            Cliente cliente = clienteRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            String token = jwtUtil.generateToken(cliente.getEmail(), cliente.getPerfil().name());
            return new LoginResponse(token);

        } catch (AuthenticationException e) {
            throw new RuntimeException("Credenciais inválidas");
        }
    }

    // 👑 REGISTRO DE ADMIN
    @PostMapping("/register-admin")
    public ResponseEntity<?> registrarAdmin(@RequestBody Cliente admin) {
        if (clienteRepository.findByEmail(admin.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Já existe um usuário com este email.");
        }

        // Senha padrão = últimos 4 dígitos do celular
        if (admin.getCelular() != null && admin.getCelular().length() >= 4) {
            String senha = admin.getCelular().substring(admin.getCelular().length() - 4);
            admin.setSenha(passwordEncoder.encode(senha));
        } else {
            return ResponseEntity.badRequest().body("Celular inválido para gerar senha.");
        }

        admin.setPerfil(Perfil.ADMIN);
        admin.setAtivo(true);

        Cliente salvo = clienteRepository.save(admin);
        return ResponseEntity.ok(salvo);
    }
}

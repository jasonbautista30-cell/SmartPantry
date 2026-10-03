using System.Security.Claims;
using BCrypt.Net;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using SmartPantryAPI.Data;
using SmartPantryAPI.DTOs;
using SmartPantryAPI.Models;
using SmartPantryAPI.Services;

namespace SmartPantryAPI.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class AuthController : ControllerBase
    {
        private readonly SmartPantryContext _context;
        private readonly IJwtService _jwtService;

        public AuthController(SmartPantryContext context, IJwtService jwtService)
        {
            _context = context;
            _jwtService = jwtService;
        }

        /// <summary>
        /// Registro de un nuevo usuario con contraseña encriptada (BCrypt).
        /// </summary>
        [HttpPost("register")]
        public async Task<IActionResult> Register([FromBody] RegisterDto dto)
        {
            if (!ModelState.IsValid)
            {
                return BadRequest(ModelState);
            }

            var correoNormalizado = dto.Correo.Trim().ToLower();

            // Verificar si el correo ya existe
            var existe = await _context.Usuarios.AnyAsync(u => u.Correo.ToLower() == correoNormalizado);
            if (existe)
            {
                return BadRequest(new { mensaje = "El correo electrónico ya se encuentra registrado." });
            }

            // Generar hash seguro con BCrypt
            string passwordHash = BCrypt.Net.BCrypt.HashPassword(dto.Password);

            var usuario = new Usuario
            {
                Nombre = dto.Nombre.Trim(),
                Correo = correoNormalizado,
                PasswordHash = passwordHash,
                Rol = string.IsNullOrWhiteSpace(dto.Rol) ? "Usuario" : dto.Rol.Trim(),
                FechaCreacion = DateTime.Now
            };

            _context.Usuarios.Add(usuario);
            await _context.SaveChangesAsync();

            // Generar token JWT inmediatamente al registrarse
            var (token, expiracion) = _jwtService.GenerateToken(usuario);

            return Ok(new AuthResponseDto
            {
                Token = token,
                IdUsuario = usuario.IdUsuario,
                Nombre = usuario.Nombre,
                Correo = usuario.Correo,
                Rol = usuario.Rol,
                Expiracion = expiracion
            });
        }

        /// <summary>
        /// Inicio de sesión de usuario y generación de token JWT.
        /// </summary>
        [HttpPost("login")]
        public async Task<IActionResult> Login([FromBody] LoginDto dto)
        {
            if (!ModelState.IsValid)
            {
                return BadRequest(ModelState);
            }

            var correoNormalizado = dto.Correo.Trim().ToLower();

            var usuario = await _context.Usuarios
                .FirstOrDefaultAsync(u => u.Correo.ToLower() == correoNormalizado);

            if (usuario == null)
            {
                return Unauthorized(new { mensaje = "Credenciales inválidas." });
            }

            // Verificar contraseña con BCrypt
            bool passwordValida = BCrypt.Net.BCrypt.Verify(dto.Password, usuario.PasswordHash);
            if (!passwordValida)
            {
                return Unauthorized(new { mensaje = "Credenciales inválidas." });
            }

            var (token, expiracion) = _jwtService.GenerateToken(usuario);

            return Ok(new AuthResponseDto
            {
                Token = token,
                IdUsuario = usuario.IdUsuario,
                Nombre = usuario.Nombre,
                Correo = usuario.Correo,
                Rol = usuario.Rol,
                Expiracion = expiracion
            });
        }

        /// <summary>
        /// Retorna la información del usuario autenticado actual.
        /// </summary>
        [HttpGet("me")]
        [Authorize]
        public async Task<IActionResult> GetProfile()
        {
            var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            if (string.IsNullOrEmpty(userIdClaim) || !int.TryParse(userIdClaim, out var idUsuario))
            {
                return Unauthorized(new { mensaje = "Token inválido o no contiene identificador de usuario." });
            }

            var usuario = await _context.Usuarios.FindAsync(idUsuario);
            if (usuario == null)
            {
                return NotFound(new { mensaje = "Usuario no encontrado." });
            }

            return Ok(new
            {
                usuario.IdUsuario,
                usuario.Nombre,
                usuario.Correo,
                usuario.Rol,
                usuario.FechaCreacion
            });
        }
    }
}

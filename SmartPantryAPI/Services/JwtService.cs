using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using Microsoft.IdentityModel.Tokens;
using SmartPantryAPI.Models;

namespace SmartPantryAPI.Services
{
    public class JwtService : IJwtService
    {
        private readonly IConfiguration _configuration;

        public JwtService(IConfiguration configuration)
        {
            _configuration = configuration;
        }

        public (string Token, DateTime Expiration) GenerateToken(Usuario usuario)
        {
            var secretKey = _configuration["Jwt:SecretKey"]
                ?? throw new InvalidOperationException("La clave secreta JWT no está configurada en appsettings.json.");
            var issuer = _configuration["Jwt:Issuer"] ?? "SmartPantryAPI";
            var audience = _configuration["Jwt:Audience"] ?? "SmartPantryWeb";
            var expiryMinutes = int.TryParse(_configuration["Jwt:ExpiryInMinutes"], out var mins) ? mins : 480;

            var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(secretKey));
            var credentials = new SigningCredentials(key, SecurityAlgorithms.HmacSha256);

            var claims = new[]
            {
                new Claim(ClaimTypes.NameIdentifier, usuario.IdUsuario.ToString()),
                new Claim(ClaimTypes.Name, usuario.Nombre),
                new Claim(ClaimTypes.Email, usuario.Correo),
                new Claim(ClaimTypes.Role, usuario.Rol ?? "Usuario"),
                new Claim(JwtRegisteredClaimNames.Jti, Guid.NewGuid().ToString())
            };

            var expiration = DateTime.UtcNow.AddMinutes(expiryMinutes);

            var tokenDescriptor = new SecurityTokenDescriptor
            {
                Subject = new ClaimsIdentity(claims),
                Expires = expiration,
                Issuer = issuer,
                Audience = audience,
                SigningCredentials = credentials
            };

            var tokenHandler = new JwtSecurityTokenHandler();
            var token = tokenHandler.CreateToken(tokenDescriptor);

            return (tokenHandler.WriteToken(token), expiration);
        }
    }
}

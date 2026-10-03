using SmartPantryAPI.Models;

namespace SmartPantryAPI.Services
{
    public interface IJwtService
    {
        (string Token, DateTime Expiration) GenerateToken(Usuario usuario);
    }
}

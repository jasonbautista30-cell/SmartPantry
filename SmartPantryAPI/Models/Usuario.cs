using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace SmartPantryAPI.Models
{
    [Table("usuarios")]
    public class Usuario
    {
        [Key]
        [Column("id_usuario")]
        public int IdUsuario { get; set; }

        [Required]
        [MaxLength(100)]
        [Column("nombre")]
        public string Nombre { get; set; } = string.Empty;

        [Required]
        [MaxLength(150)]
        [Column("correo")]
        public string Correo { get; set; } = string.Empty;

        [Required]
        [MaxLength(255)]
        [Column("password_hash")]
        public string PasswordHash { get; set; } = string.Empty;

        [Required]
        [MaxLength(30)]
        [Column("rol")]
        public string Rol { get; set; } = "Usuario";

        [Column("fecha_creacion")]
        public DateTime FechaCreacion { get; set; } = DateTime.Now;
    }
}

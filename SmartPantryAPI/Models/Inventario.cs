using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace SmartPantryAPI.Models
{
    [Table("inventario")]
    public class Inventario
    {
        [Key]
        [Column("id_inventario")]
        public int IdInventario { get; set; }

        [Required]
        [Column("id_producto")]
        public int IdProducto { get; set; }

        [Required]
        [Column("cantidad")]
        public int Cantidad { get; set; } = 0;

        [Required]
        [Column("cantidad_minima")]
        public int CantidadMinima { get; set; } = 2;

        [Column("fecha_vencimiento")]
        public DateTime? FechaVencimiento { get; set; }

        [StringLength(50)]
        [Column("ubicacion")]
        public string? Ubicacion { get; set; } = "Alacena";

        [Column("fecha_ingreso")]
        public DateTime? FechaIngreso { get; set; }

        // Navegación
        [ForeignKey("IdProducto")]
        public Producto? Producto { get; set; }
    }
}

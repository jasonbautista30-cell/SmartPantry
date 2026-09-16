using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Text.Json.Serialization;

namespace SmartPantryAPI.Models
{
    [Table("productos")]
    public class Producto
    {
        [Key]
        [Column("id_producto")]
        public int IdProducto { get; set; }

        [Column("id_categoria")]
        public int? IdCategoria { get; set; }

        [Required]
        [StringLength(100)]
        [Column("nombre")]
        public string Nombre { get; set; } = string.Empty;

        [StringLength(50)]
        [Column("codigo_barras")]
        public string? CodigoBarras { get; set; }

        [StringLength(20)]
        [Column("unidad_medida")]
        public string? UnidadMedida { get; set; } = "unidades";

        // Navegación
        [ForeignKey("IdCategoria")]
        public Categoria? Categoria { get; set; }

        [JsonIgnore]
        public ICollection<Inventario> Inventarios { get; set; } = new List<Inventario>();

        [JsonIgnore]
        public ICollection<DetalleListaCompra> DetallesListaCompra { get; set; } = new List<DetalleListaCompra>();
    }
}

using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Text.Json.Serialization;

namespace SmartPantryAPI.Models
{
    [Table("listas_compras")]
    public class ListaCompra
    {
        [Key]
        [Column("id_lista")]
        public int IdLista { get; set; }

        [StringLength(100)]
        [Column("nombre_lista")]
        public string? NombreLista { get; set; } = "Mi Lista de Compras";

        [StringLength(20)]
        [Column("estado")]
        public string? Estado { get; set; } = "PENDIENTE";

        [Column("fecha_creacion")]
        public DateTime? FechaCreacion { get; set; }

        // Navegación: una lista tiene muchos detalles
        [JsonIgnore]
        public ICollection<DetalleListaCompra> Detalles { get; set; } = new List<DetalleListaCompra>();
    }
}

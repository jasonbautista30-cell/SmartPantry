using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace SmartPantryAPI.Models
{
    [Table("detalle_lista_compras")]
    public class DetalleListaCompra
    {
        [Key]
        [Column("id_detalle")]
        public int IdDetalle { get; set; }

        [Required]
        [Column("id_lista")]
        public int IdLista { get; set; }

        [Required]
        [Column("id_producto")]
        public int IdProducto { get; set; }

        [Required]
        [Column("cantidad_a_comprar")]
        public int CantidadAComprar { get; set; } = 1;

        [Column("comprado")]
        public bool Comprado { get; set; } = false;

        // Navegación
        [ForeignKey("IdLista")]
        public ListaCompra? ListaCompra { get; set; }

        [ForeignKey("IdProducto")]
        public Producto? Producto { get; set; }
    }
}

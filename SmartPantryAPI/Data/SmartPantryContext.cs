using Microsoft.EntityFrameworkCore;
using SmartPantryAPI.Models;

namespace SmartPantryAPI.Data
{
    public class SmartPantryContext : DbContext
    {
        public SmartPantryContext(DbContextOptions<SmartPantryContext> options)
            : base(options)
        {
        }

        public DbSet<Categoria> Categorias { get; set; }
        public DbSet<Producto> Productos { get; set; }
        public DbSet<Inventario> Inventario { get; set; }
        public DbSet<ListaCompra> ListasCompras { get; set; }
        public DbSet<DetalleListaCompra> DetalleListaCompras { get; set; }
        public DbSet<Usuario> Usuarios { get; set; }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);

            // ---- usuarios ----
            modelBuilder.Entity<Usuario>(entity =>
            {
                entity.HasIndex(e => e.Correo).IsUnique();
            });

            // ---- categorias ----
            modelBuilder.Entity<Categoria>(entity =>
            {
                entity.HasIndex(e => e.Nombre).IsUnique();
            });

            // ---- productos ----
            modelBuilder.Entity<Producto>(entity =>
            {
                entity.HasIndex(e => e.CodigoBarras).IsUnique();

                entity.HasOne(p => p.Categoria)
                      .WithMany(c => c.Productos)
                      .HasForeignKey(p => p.IdCategoria)
                      .OnDelete(DeleteBehavior.SetNull);
            });

            // ---- inventario ----
            modelBuilder.Entity<Inventario>(entity =>
            {
                entity.HasOne(i => i.Producto)
                      .WithMany(p => p.Inventarios)
                      .HasForeignKey(i => i.IdProducto)
                      .OnDelete(DeleteBehavior.Cascade);
            });

            // ---- listas_compras ----
            modelBuilder.Entity<ListaCompra>(entity =>
            {
                // Sin configuración adicional
            });

            // ---- detalle_lista_compras ----
            modelBuilder.Entity<DetalleListaCompra>(entity =>
            {
                entity.HasOne(d => d.ListaCompra)
                      .WithMany(l => l.Detalles)
                      .HasForeignKey(d => d.IdLista)
                      .OnDelete(DeleteBehavior.Cascade);

                entity.HasOne(d => d.Producto)
                      .WithMany(p => p.DetallesListaCompra)
                      .HasForeignKey(d => d.IdProducto)
                      .OnDelete(DeleteBehavior.NoAction); // Evita múltiples cascade paths en SQL Server
            });
        }
    }
}

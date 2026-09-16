using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using SmartPantryAPI.Data;
using SmartPantryAPI.Models;

namespace SmartPantryAPI.Controllers
{
    [Route("api/[controller]")]
    [Route("api/DetallesListaCompras")]
    [ApiController]
    public class DetalleListaComprasController : ControllerBase
    {
        private readonly SmartPantryContext _context;

        public DetalleListaComprasController(SmartPantryContext context)
        {
            _context = context;
        }

        // GET: api/detallelistacompras?idLista=1
        [HttpGet]
        public async Task<ActionResult<IEnumerable<DetalleListaCompra>>> GetDetalles([FromQuery] int? idLista)
        {
            var query = _context.DetalleListaCompras
                .Include(d => d.Producto)
                .Include(d => d.ListaCompra)
                .AsQueryable();

            if (idLista.HasValue)
                query = query.Where(d => d.IdLista == idLista.Value);

            return await query.ToListAsync();
        }

        // GET: api/detallelistacompras/5
        [HttpGet("{id}")]
        public async Task<ActionResult<DetalleListaCompra>> GetDetalle(int id)
        {
            var detalle = await _context.DetalleListaCompras
                .Include(d => d.Producto)
                .Include(d => d.ListaCompra)
                .FirstOrDefaultAsync(d => d.IdDetalle == id);

            if (detalle == null)
                return NotFound(new { mensaje = "Detalle no encontrado." });

            return detalle;
        }

        // POST: api/detallelistacompras
        [HttpPost]
        public async Task<ActionResult<DetalleListaCompra>> PostDetalle(DetalleListaCompra detalle)
        {
            _context.DetalleListaCompras.Add(detalle);
            await _context.SaveChangesAsync();

            return CreatedAtAction(nameof(GetDetalle), new { id = detalle.IdDetalle }, detalle);
        }

        // PUT: api/detallelistacompras/5
        [HttpPut("{id}")]
        public async Task<IActionResult> PutDetalle(int id, DetalleListaCompra detalle)
        {
            if (id != detalle.IdDetalle)
                return BadRequest(new { mensaje = "El ID no coincide." });

            _context.Entry(detalle).State = EntityState.Modified;

            try
            {
                await _context.SaveChangesAsync();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!await _context.DetalleListaCompras.AnyAsync(e => e.IdDetalle == id))
                    return NotFound(new { mensaje = "Detalle no encontrado." });
                throw;
            }

            return NoContent();
        }

        // PATCH: api/detallelistacompras/5/toggle-comprado
        [HttpPatch("{id}/toggle-comprado")]
        public async Task<IActionResult> ToggleComprado(int id)
        {
            var detalle = await _context.DetalleListaCompras.FindAsync(id);

            if (detalle == null)
                return NotFound(new { mensaje = "Detalle no encontrado." });

            detalle.Comprado = !detalle.Comprado;
            await _context.SaveChangesAsync();

            return Ok(new { mensaje = "Estado de comprado actualizado.", detalle });
        }

        // DELETE: api/detallelistacompras/5
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteDetalle(int id)
        {
            var detalle = await _context.DetalleListaCompras.FindAsync(id);

            if (detalle == null)
                return NotFound(new { mensaje = "Detalle no encontrado." });

            _context.DetalleListaCompras.Remove(detalle);
            await _context.SaveChangesAsync();

            return NoContent();
        }
    }
}

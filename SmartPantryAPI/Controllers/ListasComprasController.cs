using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using SmartPantryAPI.Data;
using SmartPantryAPI.Models;

namespace SmartPantryAPI.Controllers
{
    [Authorize]
    [Route("api/[controller]")]
    [ApiController]
    public class ListasComprasController : ControllerBase
    {
        private readonly SmartPantryContext _context;

        public ListasComprasController(SmartPantryContext context)
        {
            _context = context;
        }

        // GET: api/listascompras
        [HttpGet]
        public async Task<ActionResult<IEnumerable<ListaCompra>>> GetListasCompras()
        {
            return await _context.ListasCompras.ToListAsync();
        }

        // GET: api/listascompras/5
        [HttpGet("{id}")]
        public async Task<ActionResult<ListaCompra>> GetListaCompra(int id)
        {
            var lista = await _context.ListasCompras
                .Include(l => l.Detalles)
                    .ThenInclude(d => d.Producto)
                .FirstOrDefaultAsync(l => l.IdLista == id);

            if (lista == null)
                return NotFound(new { mensaje = "Lista de compras no encontrada." });

            return lista;
        }

        // POST: api/listascompras
        [HttpPost]
        public async Task<ActionResult<ListaCompra>> PostListaCompra(ListaCompra lista)
        {
            _context.ListasCompras.Add(lista);
            await _context.SaveChangesAsync();

            return CreatedAtAction(nameof(GetListaCompra), new { id = lista.IdLista }, lista);
        }

        // PUT: api/listascompras/5
        [HttpPut("{id}")]
        public async Task<IActionResult> PutListaCompra(int id, ListaCompra lista)
        {
            if (id != lista.IdLista)
                return BadRequest(new { mensaje = "El ID no coincide." });

            _context.Entry(lista).State = EntityState.Modified;

            try
            {
                await _context.SaveChangesAsync();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!await _context.ListasCompras.AnyAsync(e => e.IdLista == id))
                    return NotFound(new { mensaje = "Lista de compras no encontrada." });
                throw;
            }

            return NoContent();
        }

        // PATCH: api/listascompras/5/completar
        [HttpPatch("{id}/completar")]
        public async Task<IActionResult> CompletarLista(int id)
        {
            var lista = await _context.ListasCompras.FindAsync(id);

            if (lista == null)
                return NotFound(new { mensaje = "Lista de compras no encontrada." });

            lista.Estado = "COMPLETADA";
            await _context.SaveChangesAsync();

            return Ok(new { mensaje = "Lista marcada como completada.", lista });
        }

        // DELETE: api/listascompras/5
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteListaCompra(int id)
        {
            var lista = await _context.ListasCompras.FindAsync(id);

            if (lista == null)
                return NotFound(new { mensaje = "Lista de compras no encontrada." });

            _context.ListasCompras.Remove(lista);
            await _context.SaveChangesAsync();

            return NoContent();
        }
    }
}

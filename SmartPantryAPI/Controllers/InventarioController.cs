using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using SmartPantryAPI.Data;
using SmartPantryAPI.Models;

namespace SmartPantryAPI.Controllers
{
    [Route("api/[controller]")]
    [Route("api/Inventarios")]
    [ApiController]
    public class InventarioController : ControllerBase
    {
        private readonly SmartPantryContext _context;

        public InventarioController(SmartPantryContext context)
        {
            _context = context;
        }

        // GET: api/inventario
        [HttpGet]
        public async Task<ActionResult<IEnumerable<Inventario>>> GetInventario()
        {
            return await _context.Inventario
                .Include(i => i.Producto)
                .ToListAsync();
        }

        // GET: api/inventario/5
        [HttpGet("{id}")]
        public async Task<ActionResult<Inventario>> GetInventarioItem(int id)
        {
            var item = await _context.Inventario
                .Include(i => i.Producto)
                .FirstOrDefaultAsync(i => i.IdInventario == id);

            if (item == null)
                return NotFound(new { mensaje = "Registro de inventario no encontrado." });

            return item;
        }

        // GET: api/inventario/bajo-stock
        // Devuelve productos cuya cantidad es menor a la cantidad mínima
        [HttpGet("bajo-stock")]
        public async Task<ActionResult<IEnumerable<Inventario>>> GetBajoStock()
        {
            var items = await _context.Inventario
                .Include(i => i.Producto)
                .Where(i => i.Cantidad < i.CantidadMinima)
                .ToListAsync();

            return Ok(items);
        }

        // GET: api/inventario/por-vencer?dias=7
        // Devuelve productos que vencen dentro de los próximos N días
        [HttpGet("por-vencer")]
        public async Task<ActionResult<IEnumerable<Inventario>>> GetPorVencer([FromQuery] int dias = 7)
        {
            var fechaLimite = DateTime.Today.AddDays(dias);

            var items = await _context.Inventario
                .Include(i => i.Producto)
                .Where(i => i.FechaVencimiento != null && i.FechaVencimiento <= fechaLimite)
                .OrderBy(i => i.FechaVencimiento)
                .ToListAsync();

            return Ok(items);
        }

        // POST: api/inventario
        [HttpPost]
        public async Task<ActionResult<Inventario>> PostInventario(Inventario inventario)
        {
            _context.Inventario.Add(inventario);
            await _context.SaveChangesAsync();

            return CreatedAtAction(nameof(GetInventarioItem), new { id = inventario.IdInventario }, inventario);
        }

        // PUT: api/inventario/5
        [HttpPut("{id}")]
        public async Task<IActionResult> PutInventario(int id, Inventario inventario)
        {
            if (id != inventario.IdInventario)
                return BadRequest(new { mensaje = "El ID no coincide." });

            _context.Entry(inventario).State = EntityState.Modified;

            try
            {
                await _context.SaveChangesAsync();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!await _context.Inventario.AnyAsync(e => e.IdInventario == id))
                    return NotFound(new { mensaje = "Registro de inventario no encontrado." });
                throw;
            }

            return NoContent();
        }

        // DELETE: api/inventario/5
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteInventario(int id)
        {
            var item = await _context.Inventario.FindAsync(id);

            if (item == null)
                return NotFound(new { mensaje = "Registro de inventario no encontrado." });

            _context.Inventario.Remove(item);
            await _context.SaveChangesAsync();

            return NoContent();
        }
    }
}

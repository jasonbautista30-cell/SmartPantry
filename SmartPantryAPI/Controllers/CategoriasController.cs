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
    public class CategoriasController : ControllerBase
    {
        private readonly SmartPantryContext _context;

        public CategoriasController(SmartPantryContext context)
        {
            _context = context;
        }

        // GET: api/categorias
        [HttpGet]
        public async Task<ActionResult<IEnumerable<Categoria>>> GetCategorias()
        {
            return await _context.Categorias.ToListAsync();
        }

        // GET: api/categorias/5
        [HttpGet("{id}")]
        public async Task<ActionResult<Categoria>> GetCategoria(int id)
        {
            var categoria = await _context.Categorias.FindAsync(id);

            if (categoria == null)
                return NotFound(new { mensaje = "Categoría no encontrada." });

            return categoria;
        }

        // POST: api/categorias
        [HttpPost]
        public async Task<ActionResult<Categoria>> PostCategoria(Categoria categoria)
        {
            _context.Categorias.Add(categoria);
            await _context.SaveChangesAsync();

            return CreatedAtAction(nameof(GetCategoria), new { id = categoria.IdCategoria }, categoria);
        }

        // PUT: api/categorias/5
        [HttpPut("{id}")]
        public async Task<IActionResult> PutCategoria(int id, Categoria categoria)
        {
            if (id != categoria.IdCategoria)
                return BadRequest(new { mensaje = "El ID no coincide." });

            _context.Entry(categoria).State = EntityState.Modified;

            try
            {
                await _context.SaveChangesAsync();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!await _context.Categorias.AnyAsync(e => e.IdCategoria == id))
                    return NotFound(new { mensaje = "Categoría no encontrada." });
                throw;
            }

            return NoContent();
        }

        // DELETE: api/categorias/5
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteCategoria(int id)
        {
            var categoria = await _context.Categorias.FindAsync(id);

            if (categoria == null)
                return NotFound(new { mensaje = "Categoría no encontrada." });

            _context.Categorias.Remove(categoria);
            await _context.SaveChangesAsync();

            return NoContent();
        }
    }
}

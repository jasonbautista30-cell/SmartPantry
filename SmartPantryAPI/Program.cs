using Microsoft.EntityFrameworkCore;
using SmartPantryAPI.Data;

var builder = WebApplication.CreateBuilder(args);

// ----- Servicios -----

// Entity Framework Core con SQL Server
builder.Services.AddDbContext<SmartPantryContext>(options =>
    options.UseSqlServer(builder.Configuration.GetConnectionString("SmartPantryConnection")));

// Controladores con formato JSON
builder.Services.AddControllers()
    .AddJsonOptions(options =>
    {
        options.JsonSerializerOptions.ReferenceHandler = System.Text.Json.Serialization.ReferenceHandler.IgnoreCycles;
        options.JsonSerializerOptions.DefaultIgnoreCondition = System.Text.Json.Serialization.JsonIgnoreCondition.WhenWritingNull;
    });

// Configuración CORS para permitir conexiones desde el cliente React
builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowAll",
        policy => policy.AllowAnyOrigin()
                        .AllowAnyMethod()
                        .AllowAnyHeader());
});

var app = builder.Build();

// ----- Middleware -----

app.UseCors("AllowAll");
app.UseAuthorization();
app.MapControllers();

app.Run();

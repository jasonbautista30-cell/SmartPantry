using System.Text;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using Microsoft.OpenApi;
using SmartPantryAPI.Data;
using SmartPantryAPI.Services;

var builder = WebApplication.CreateBuilder(args);

// ======================================================
// BASE DE DATOS
// ======================================================

builder.Services.AddDbContext<SmartPantryContext>(options =>
    options.UseSqlServer(
        builder.Configuration.GetConnectionString("SmartPantryConnection")
    )
);

// ======================================================
// SERVICIOS
// ======================================================

builder.Services.AddScoped<IJwtService, JwtService>();

// ======================================================
// REDIRECCIÓN HTTPS
// ======================================================

// Fuerza las solicitudes HTTP a utilizar HTTPS.
// Somee ya tiene el certificado SSL configurado en el puerto 443.
builder.Services.AddHttpsRedirection(options =>
{
    options.RedirectStatusCode = StatusCodes.Status308PermanentRedirect;
    options.HttpsPort = 443;
});

// ======================================================
// AUTENTICACIÓN JWT
// ======================================================

var secretKey = builder.Configuration["Jwt:SecretKey"]
    ?? throw new InvalidOperationException("Jwt:SecretKey no configurada.");

var issuer = builder.Configuration["Jwt:Issuer"] ?? "SmartPantryAPI";
var audience = builder.Configuration["Jwt:Audience"] ?? "SmartPantryWeb";

builder.Services
    .AddAuthentication(options =>
    {
        options.DefaultAuthenticateScheme =
            JwtBearerDefaults.AuthenticationScheme;

        options.DefaultChallengeScheme =
            JwtBearerDefaults.AuthenticationScheme;
    })
    .AddJwtBearer(options =>
    {
        options.TokenValidationParameters = new TokenValidationParameters
        {
            ValidateIssuer = true,
            ValidateAudience = true,
            ValidateLifetime = true,
            ValidateIssuerSigningKey = true,

            ValidIssuer = issuer,
            ValidAudience = audience,

            IssuerSigningKey = new SymmetricSecurityKey(
                Encoding.UTF8.GetBytes(secretKey)
            )
        };
    });

// ======================================================
// CONTROLADORES / JSON
// ======================================================

builder.Services
    .AddControllers()
    .AddJsonOptions(options =>
    {
        options.JsonSerializerOptions.ReferenceHandler =
            System.Text.Json.Serialization.ReferenceHandler.IgnoreCycles;

        options.JsonSerializerOptions.DefaultIgnoreCondition =
            System.Text.Json.Serialization.JsonIgnoreCondition.WhenWritingNull;
    });

// ======================================================
// CORS
// ======================================================

builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowAll",
        policy => policy
            .AllowAnyOrigin()
            .AllowAnyMethod()
            .AllowAnyHeader());
});

// ======================================================
// SWAGGER
// ======================================================

builder.Services.AddEndpointsApiExplorer();

builder.Services.AddSwaggerGen(c =>
{
    c.SwaggerDoc("v1", new OpenApiInfo
    {
        Title = "SmartPantryAPI",
        Version = "v1"
    });

    var securityScheme = new OpenApiSecurityScheme
    {
        Name = "Authorization",
        Description = "Ingrese únicamente su token JWT.",
        In = ParameterLocation.Header,
        Type = SecuritySchemeType.Http,
        Scheme = "bearer",
        BearerFormat = "JWT"
    };

    c.AddSecurityDefinition("Bearer", securityScheme);

    c.AddSecurityRequirement(doc => new OpenApiSecurityRequirement
    {
        {
            new OpenApiSecuritySchemeReference("Bearer", doc),
            new List<string>()
        }
    });
});

// ======================================================
// CREAR APLICACIÓN
// ======================================================

var app = builder.Build();

// ======================================================
// HTTPS
// ======================================================

// Cualquier solicitud por HTTP será redirigida automáticamente a HTTPS.
app.UseHttpsRedirection();

// ======================================================
// SWAGGER
// ======================================================

app.UseSwagger();

app.UseSwaggerUI(options =>
{
    options.SwaggerEndpoint(
        "/swagger/v1/swagger.json",
        "SmartPantryAPI v1"
    );
});

// ======================================================
// FRONTEND REACT / VITE
// ======================================================

// Permite que "/" busque automáticamente wwwroot/index.html.
app.UseDefaultFiles();

// Permite servir index.html, JavaScript, CSS, imágenes, etc.
app.UseStaticFiles();

// ======================================================
// MIDDLEWARE
// ======================================================

app.UseCors("AllowAll");

app.UseAuthentication();
app.UseAuthorization();

// ======================================================
// API
// ======================================================

app.MapControllers();

// Endpoint para comprobar que la API está funcionando.
app.MapGet("/api/status", () => Results.Ok(new
{
    api = "SmartPantryAPI",
    estado = "Funcionando correctamente",
    swagger = "/swagger/index.html"
}));

// ======================================================
// REACT ROUTER
// ======================================================

// Cualquier ruta que no pertenezca a la API devuelve index.html
// para que React pueda manejar la navegación.
app.MapFallbackToFile("index.html");

// ======================================================
// EJECUTAR
// ======================================================

app.Run();
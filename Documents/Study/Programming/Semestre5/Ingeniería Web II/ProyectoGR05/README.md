# Actividades - Ingeniería Web II

## Enunciado Caso de Estudio

La Institución Universitaria Digital de Antioquia necesita crear una Aplicación Web (“desde cero”), para publicar películas en modo Administrador, con el fin de ofrecerle a docentes, estudiantes, colaboradores y público en general una web de entretenimiento donde puedan ver películas online de forma gratuita y sin acceso o registro requerido (por ahora).

Para este reto, la Universidad contrata a tu equipo de trabajo como Ingenieros Web.

Ya existe información relevante definida por los directivos, quienes desean una aplicación tipo “Cuevana” (sin piratería), ya que la Universidad asumirá el pago de las licencias de las películas.

Captura Web Cuevana. Tomado de: https://cuevana3.nu/

Además, el equipo de tecnología del Alma Mater realizó un análisis previo y definió que la Aplicación se desarrollará con una **arquitectura monolítica**, trabajando de manera separada el Frontend del Backend, con los siguientes cinco (5) módulos:

### ❖ Módulo de Género

Permitirá registrar y editar los géneros de películas para la Aplicación Web.

Inicialmente se tendrán: **acción, aventura, ciencia ficción, drama y terror**, pero podrán agregarse otros a medida que se necesiten.

Los directivos solicitan que una película o serie se clasifique en **un único género** por ahora.

Se guardará la siguiente información:

1. Nombre  
2. Estado (Activo o Inactivo)  
3. Fecha creación  
4. Fecha actualización  
5. Descripción  

### ❖ Módulo de Director

Permitirá registrar y editar el director principal de la producción (solo uno, aunque puedan existir varios).

Se almacenará la siguiente información:

1. Nombres  
2. Estado (Activo o Inactivo)  
3. Fecha de creación  
4. Fecha de actualización  

### ❖ Módulo Productora

Permitirá registrar y editar la productora principal de la producción (Disney, Warner, Paramount, MGM, …).

Se guardará la siguiente información:

1. Nombre de la productora  
2. Estado (Activo o Inactivo)  
3. Fecha de creación  
4. Fecha de actualización  
5. Slogan  
6. Descripción  

### ❖ Módulo Tipo

Permitirá registrar los tipos de multimedia. Inicialmente se tendrán: **serie y película**, pero podrán gestionarse otros tipos en el futuro.

Se almacenará:

1. Nombre  
2. Fecha de creación  
3. Fecha de actualización  
4. Descripción  

### ❖ Módulo de Media (Películas y series)

Este módulo se encargará de gestionar (agregado, edición, borrado, consulta) las producciones (películas y series, por ahora).

Se almacenará la siguiente información:

1. Serial: único  
2. Título  
3. Sinopsis  
4. URL de la película: único  
5. Imagen o foto de portada  
6. Fecha de creación  
7. Fecha de actualización  
8. Año de estreno  
9. Género principal: solo géneros activos del Módulo de Género  
10. Director principal: solo directores activos del Módulo de Director  
11. Productora: solo productoras activas del Módulo de Productora  
12. Tipo: solo tipos definidos en el Módulo de Tipo  

---

✅ **Recuerda:** asumirás el rol de Ingeniero Web en este proyecto. Tus tareas principales incluyen comprender el desafío propuesto, realizar análisis y diseño ágil, y actuar como FullStack:

- Desarrollar el **Backend** (junto con su base de datos)
- Desarrollar el **Frontend**
- Integrar ambas capas y realizar el despliegue

El objetivo de la aplicación web es la gestión de películas con enfoque de **Administrador**, permitiendo agregarlas a la plataforma.

Actualmente, **no se requiere** implementación de registro de usuarios ni módulos de seguridad.  
Por ahora, la aplicación **no está orientada a usuarios finales** (estudiantes, docentes, colaboradores o público en general).

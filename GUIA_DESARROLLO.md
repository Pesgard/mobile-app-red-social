# 📱 Guía de Desarrollo - Connecta Social Network

## 🎯 Objetivo
Desarrollar la aplicación móvil Android siguiendo Clean Architecture + MVVM, implementando funcionalidad offline-first con sincronización automática.

---

## 📋 Fases de Desarrollo

### **FASE 0: Configuración Base y Utilidades** ✅ (Fundación)
*Tiempo estimado: 1-2 días*

#### Objetivo
Configurar las utilidades básicas que se usarán en toda la aplicación.

#### Tareas

1. **Implementar `Resource.kt`** (Manejo de estados)
   ```kotlin
   sealed class Resource<out T> {
       data class Success<T>(val data: T) : Resource<T>()
       data class Error(val message: String, val exception: Exception? = null) : Resource<Nothing>()
       object Loading : Resource<Nothing>()
   }
   ```

2. **Implementar `Constants.kt`** (Constantes de la app)
   - Base URL de la API
   - Keys para DataStore
   - Nombres de tablas
   - Códigos de error comunes
   - Timeouts

3. **Implementar `NetworkMonitor.kt`** (Monitoreo de conexión)
   - Usar ConnectivityManager
   - Flow de estado de conexión
   - Función helper `isOnline(): Boolean`

4. **Crear Extension Functions útiles**
   - `String.isValidEmail()`
   - `String.isValidPassword()`
   - `Long.toDateString()`
   - Conversiones entre Entities ↔ Domain Models ↔ DTOs

#### ✅ Checklist
- [ ] Resource.kt implementado
- [ ] Constants.kt con todas las constantes necesarias
- [ ] NetworkMonitor funcional
- [ ] Extension functions creadas y probadas

---

### **FASE 1: Base de Datos Local (Room)** 🗄️
*Tiempo estimado: 2-3 días*

#### Objetivo
Configurar Room Database con todas las entidades, DAOs y relaciones necesarias.

#### Orden de Implementación

1. **Entidades (Entities)**
   - `UserEntity.kt` - Información del usuario
     - Campos: id, email, name, lastName, alias, passwordHash, avatarUrl, phone, address, createdAt, updatedAt
     - Primary Key: id (Long)
   
   - `PostEntity.kt` - Publicaciones
     - Campos: id, userId, title, description, images (JSON o lista), likes, dislikes, createdAt, updatedAt, synced (Boolean), serverId (String?)
     - Foreign Key: userId → UserEntity
     - Índices: userId, createdAt, synced
   
   - `CommentEntity.kt` - Comentarios
     - Campos: id, postId, userId, parentCommentId (nullable), text, likes, createdAt, updatedAt, synced, serverId
     - Foreign Keys: postId → PostEntity, userId → UserEntity, parentCommentId → CommentEntity
     - Índices: postId, parentCommentId
   
   - `FavoriteEntity.kt` - Favoritos
     - Campos: id, userId, postId, createdAt
     - Primary Key compuesto: userId + postId
     - Índices: userId, postId

2. **DAOs (Data Access Objects)**
   - `UserDao.kt`
     - `getUserById(id: Long): Flow<UserEntity?>`
     - `getCurrentUser(): Flow<UserEntity?>`
     - `insertUser(user: UserEntity): Long`
     - `updateUser(user: UserEntity)`
     - `deleteUser(id: Long)`
   
   - `PostDao.kt`
     - `getAllPosts(): Flow<List<PostEntity>>`
     - `getPostById(id: Long): Flow<PostEntity?>`
     - `getPostsByUserId(userId: Long): Flow<List<PostEntity>>`
     - `getUnsyncedPosts(): Flow<List<PostEntity>>`
     - `insertPost(post: PostEntity): Long`
     - `updatePost(post: PostEntity)`
     - `deletePost(id: Long)`
     - `searchPosts(query: String): Flow<List<PostEntity>>`
   
   - `CommentDao.kt`
     - `getCommentsByPostId(postId: Long): Flow<List<CommentEntity>>`
     - `getUnsyncedComments(): Flow<List<CommentEntity>>`
     - `insertComment(comment: CommentEntity): Long`
     - `updateComment(comment: CommentEntity)`
     - `deleteComment(id: Long)`
   
   - `FavoriteDao.kt`
     - `getFavoritesByUserId(userId: Long): Flow<List<FavoriteEntity>>`
     - `isFavorite(userId: Long, postId: Long): Flow<Boolean>`
     - `insertFavorite(favorite: FavoriteEntity)`
     - `deleteFavorite(userId: Long, postId: Long)`

3. **AppDatabase.kt**
   - Configurar Room con todas las entidades
   - Crear instancia Singleton (usando Hilt)
   - Migraciones si son necesarias
   - Configurar callbacks para debugging

4. **DatabaseModule.kt (Hilt)**
   - Proporcionar instancia de AppDatabase
   - Proporcionar todos los DAOs

#### ✅ Checklist
- [ ] Todas las entidades creadas con las relaciones correctas
- [ ] Todos los DAOs implementados con queries necesarias
- [ ] AppDatabase configurado y funcionando
- [ ] DatabaseModule inyectando correctamente
- [ ] Pruebas básicas de inserción/consulta manuales

---

### **FASE 2: Modelos de Dominio** 🏗️
*Tiempo estimado: 1 día*

#### Objetivo
Definir los modelos de dominio (puros, sin anotaciones de Room/Retrofit).

#### Implementación

1. **User.kt**
   ```kotlin
   data class User(
       val id: Long = 0,
       val email: String,
       val name: String,
       val lastName: String,
       val alias: String,
       val avatarUrl: String? = null,
       val phone: String? = null,
       val address: String? = null,
       val createdAt: Long = System.currentTimeMillis(),
       val updatedAt: Long = System.currentTimeMillis()
   )
   ```

2. **Post.kt**
   ```kotlin
   data class Post(
       val id: Long = 0,
       val userId: Long,
       val user: User? = null, // Para mostrar info del autor
       val title: String,
       val description: String,
       val images: List<String> = emptyList(),
       val likes: Int = 0,
       val dislikes: Int = 0,
       val commentsCount: Int = 0,
       val isLiked: Boolean = false,
       val isFavorite: Boolean = false,
       val createdAt: Long = System.currentTimeMillis(),
       val updatedAt: Long = System.currentTimeMillis(),
       val synced: Boolean = true,
       val serverId: String? = null
   )
   ```

3. **Comment.kt**
   ```kotlin
   data class Comment(
       val id: Long = 0,
       val postId: Long,
       val userId: Long,
       val user: User? = null,
       val parentCommentId: Long? = null,
       val replies: List<Comment> = emptyList(),
       val text: String,
       val likes: Int = 0,
       val isLiked: Boolean = false,
       val createdAt: Long = System.currentTimeMillis(),
       val updatedAt: Long = System.currentTimeMillis(),
       val synced: Boolean = true,
       val serverId: String? = null
   )
   ```

4. **Extension Functions de Conversión**
   - `UserEntity.toDomain(): User`
   - `User.toEntity(): UserEntity`
   - `PostEntity.toDomain(user: UserEntity?): Post`
   - `Post.toEntity(userId: Long): PostEntity`
   - Similar para Comment y DTOs

#### ✅ Checklist
- [ ] Modelos de dominio creados
- [ ] Extension functions de conversión implementadas
- [ ] Modelos son data classes inmutables

---

### **FASE 3: API y DTOs** 🌐
*Tiempo estimado: 2-3 días*

#### Objetivo
Configurar Retrofit, definir DTOs y crear ApiService.

#### Orden de Implementación

1. **DTOs (Data Transfer Objects)**
   - `UserDto.kt` - Respuestas de la API para usuarios
   - `PostDto.kt` - Respuestas de la API para publicaciones
   - `CommentDto.kt` - Respuestas de la API para comentarios
   - `LoginRequest.kt` - Request para login
   - `RegisterRequest.kt` - Request para registro
   - `CreatePostRequest.kt` - Request para crear post
   - `CreateCommentRequest.kt` - Request para crear comentario

2. **ApiService.kt**
   - Endpoints de autenticación:
     - `POST /api/auth/register`
     - `POST /api/auth/login`
     - `GET /api/auth/me`
     - `PUT /api/auth/profile`
   - Endpoints de posts:
     - `GET /api/posts`
     - `GET /api/posts/{id}`
     - `POST /api/posts`
     - `PUT /api/posts/{id}`
     - `DELETE /api/posts/{id}`
   - Endpoints de comentarios:
     - `GET /api/posts/{postId}/comments`
     - `POST /api/posts/{postId}/comments`
     - `PUT /api/comments/{id}`
     - `DELETE /api/comments/{id}`
   - Endpoints de likes/favorites:
     - `POST /api/posts/{id}/like`
     - `POST /api/posts/{id}/favorite`
     - `DELETE /api/posts/{id}/favorite`

3. **NetworkModule.kt (Hilt)**
   - Configurar OkHttpClient con:
     - Interceptor para agregar token de autenticación
     - Logging interceptor (solo en debug)
     - Timeout configurado
   - Configurar Retrofit con Moshi
   - Proporcionar instancia de ApiService
   - Manejo de errores HTTP (4xx, 5xx)

4. **Interceptores**
   - AuthInterceptor: Agregar token JWT a requests
   - ErrorInterceptor: Convertir errores HTTP a excepciones custom

#### ✅ Checklist
- [ ] Todos los DTOs creados y mapeados correctamente
- [ ] ApiService con todos los endpoints necesarios
- [ ] NetworkModule configurado
- [ ] Interceptores funcionando
- [ ] Pruebas de conectividad con el backend real

---

### **FASE 4: Repositorios (Domain Layer)** 🔄
*Tiempo estimado: 2-3 días*

#### Objetivo
Implementar el patrón Repository que abstrae las fuentes de datos.

#### Orden de Implementación

1. **Interfaces de Repositorio (Domain Layer)**
   - `AuthRepository.kt`
     - `suspend fun login(email: String, password: String): Resource<User>`
     - `suspend fun register(user: User, password: String): Resource<User>`
     - `suspend fun getCurrentUser(): Flow<User?>`
     - `suspend fun updateProfile(user: User): Resource<User>`
     - `suspend fun logout()`
   
   - `PostRepository.kt`
     - `fun getPosts(): Flow<List<Post>>`
     - `fun getPostById(id: Long): Flow<Post?>`
     - `suspend fun createPost(post: Post): Resource<Post>`
     - `suspend fun updatePost(post: Post): Resource<Post>`
     - `suspend fun deletePost(id: Long): Resource<Unit>`
     - `suspend fun likePost(postId: Long): Resource<Unit>`
     - `suspend fun favoritePost(postId: Long): Resource<Unit>`
     - `suspend fun unfavoritePost(postId: Long): Resource<Unit>`
     - `suspend fun searchPosts(query: String): Flow<List<Post>>`
     - `suspend fun syncPosts(): Resource<Unit>`
   
   - `CommentRepository.kt`
     - `fun getCommentsByPostId(postId: Long): Flow<List<Comment>>`
     - `suspend fun createComment(comment: Comment): Resource<Comment>`
     - `suspend fun updateComment(comment: Comment): Resource<Comment>`
     - `suspend fun deleteComment(id: Long): Resource<Unit>`
     - `suspend fun likeComment(commentId: Long): Resource<Unit>`
     - `suspend fun syncComments(): Resource<Unit>`

2. **Implementaciones (Data Layer)**
   
   **AuthRepositoryImpl.kt** (Offline-first):
   ```kotlin
   override suspend fun login(email: String, password: String): Resource<User> {
       return try {
           // 1. Intentar login en API
           val response = apiService.login(LoginRequest(email, password))
           val user = response.toDomain()
           
           // 2. Guardar usuario en DB local
           userDao.insertUser(user.toEntity())
           
           // 3. Guardar token en SessionManager
           sessionManager.saveToken(response.token)
           sessionManager.saveUserId(user.id)
           
           Resource.Success(user)
       } catch (e: Exception) {
           Resource.Error("Error al iniciar sesión", e)
       }
   }
   ```
   
   **PostRepositoryImpl.kt** (Offline-first):
   - `createPost()`: Guardar local primero, luego intentar sync
   - `getPosts()`: Leer de DB local, luego intentar actualizar desde API si hay conexión
   - `syncPosts()`: Sincronizar posts no sincronizados

3. **SessionManager.kt** (DataStore)
   - Guardar/leer token JWT
   - Guardar/leer userId actual
   - Función `isLoggedIn(): Flow<Boolean>`
   - Función `clearSession()`

4. **AppModule.kt (Hilt)**
   - Proporcionar instancias de repositorios
   - Proporcionar SessionManager

#### ✅ Checklist
- [ ] Interfaces de repositorio definidas
- [ ] Implementaciones con lógica offline-first
- [ ] SessionManager funcionando
- [ ] Todos los repositorios inyectados correctamente
- [ ] Pruebas básicas de flujos offline/online

---

### **FASE 5: Autenticación (UI)** 🔐
*Tiempo estimado: 3-4 días*

#### Objetivo
Implementar flujo completo de autenticación: Welcome, Login, Register.

#### Orden de Implementación

1. **LoginViewModel.kt**
   - Estados: `LoginUiState`
     - `email: String`
     - `password: String`
     - `isLoading: Boolean`
     - `error: String?`
     - `isPasswordVisible: Boolean`
   - Funciones:
     - `login()`
     - `togglePasswordVisibility()`
     - Validación de campos

2. **LoginScreen.kt**
   - Formulario con campos email y password
   - Toggle de visibilidad de contraseña
   - Botón de login
   - Link "Forgot Password?" (opcional)
   - Manejo de estados (Loading, Error, Success)
   - Validaciones en tiempo real
   - Navegación a Register
   - Navegación a Feed después de login exitoso

3. **RegisterViewModel.kt**
   - Estados: `RegisterUiState`
     - Todos los campos del formulario
     - `isLoading: Boolean`
     - `error: String?`
     - `isPasswordVisible: Boolean`
   - Funciones:
     - `register()`
     - `togglePasswordVisibility()`
     - Validación de contraseña (≥10 chars, mayúscula, minúscula, número)

4. **RegisterScreen.kt**
   - Formulario completo de registro
   - Selector de imagen para avatar
   - Validaciones en tiempo real
   - Manejo de errores
   - Navegación a Login
   - Navegación a Feed después de registro exitoso

5. **WelcomeScreen.kt** (Opcional - puede estar integrado en Login)
   - Segmented control: Login / Register
   - Mostrar LoginScreen o RegisterScreen según selección

6. **Componentes Reutilizables**
   - `AuthTextField.kt` - Campo de texto con validación
   - `PasswordTextField.kt` - Campo de contraseña con toggle
   - `PrimaryButton.kt` - Botón primario
   - `LoadingIndicator.kt` - Indicador de carga

#### ✅ Checklist
- [ ] LoginScreen funcional y conectado al backend
- [ ] RegisterScreen funcional con todas las validaciones
- [ ] Manejo de errores apropiado
- [ ] Navegación funcionando
- [ ] SessionManager guardando datos correctamente
- [ ] UI siguiendo el diseño de los mockups

---

### **FASE 6: Navegación** 🧭
*Tiempo estimado: 1-2 días*

#### Objetivo
Configurar navegación completa de la app con Jetpack Navigation Compose.

#### Implementación

1. **NavGraph.kt**
   - Rutas (Sealed class o String constants):
     ```kotlin
     sealed class Screen(val route: String) {
         object Welcome : Screen("welcome")
         object Login : Screen("login")
         object Register : Screen("register")
         object Feed : Screen("feed")
         object PostDetail : Screen("post_detail/{postId}")
         object CreatePost : Screen("create_post")
         object EditPost : Screen("edit_post/{postId}")
         object Profile : Screen("profile/{userId}")
         object Favorites : Screen("favorites")
         object Settings : Screen("settings")
         object EditProfile : Screen("edit_profile")
     }
     ```
   
   - Navigation composable con NavHost
   - Navegación entre pantallas
   - Argumentos de navegación
   - Deep links (opcional)

2. **Navegación con argumentos**
   - PostDetail con postId
   - Profile con userId
   - EditPost con postId

3. **Bottom Navigation Bar** (si aplica)
   - Feed
   - Favorites
   - Create Post
   - Profile

4. **Navegación condicional**
   - Si está logueado → Feed
   - Si no está logueado → Welcome/Login

#### ✅ Checklist
- [ ] NavGraph configurado con todas las rutas
- [ ] Navegación funcionando entre pantallas
- [ ] Argumentos pasándose correctamente
- [ ] Navegación condicional según estado de autenticación

---

### **FASE 7: Feed de Publicaciones** 📰
*Tiempo estimado: 3-4 días*

#### Objetivo
Mostrar lista de publicaciones con funcionalidad básica.

#### Orden de Implementación

1. **FeedViewModel.kt**
   - Estados: `FeedUiState`
     - `posts: List<Post>`
     - `isLoading: Boolean`
     - `error: String?`
     - `isRefreshing: Boolean`
   - Funciones:
     - `loadPosts()`
     - `refreshPosts()`
     - `onPostLike(postId: Long)`
     - `onPostFavorite(postId: Long)`
     - `searchPosts(query: String)`

2. **FeedScreen.kt**
   - Top App Bar con:
     - Título "Connecta"
     - Botón de búsqueda
     - Botón de crear post
     - Botón de perfil
   - Lista de posts (LazyColumn)
   - Pull to refresh
   - Indicador de carga inicial
   - Manejo de estados vacíos
   - Manejo de errores

3. **PostCard.kt** (Componente reutilizable)
   - Header con avatar y nombre del usuario
   - Imágenes del post (carousel si hay múltiples)
   - Título y descripción
   - Botones de like/dislike
   - Botón de favorito
   - Contador de comentarios
   - Fecha de publicación
   - Acciones: Ver detalles, Editar (si es propio), Eliminar (si es propio)

4. **ImageCarousel.kt** (Componente)
   - Mostrar múltiples imágenes
   - Indicadores de página
   - Zoom (opcional)

5. **Integración con repositorio**
   - Cargar posts desde PostRepository
   - Observar cambios en tiempo real (Flow)
   - Sincronizar con servidor si hay conexión

#### ✅ Checklist
- [ ] FeedScreen mostrando lista de posts
- [ ] PostCard con todos los elementos
- [ ] Funcionalidad de like/dislike
- [ ] Funcionalidad de favoritos
- [ ] Pull to refresh funcionando
- [ ] Búsqueda básica funcionando
- [ ] Navegación a PostDetail funcionando

---

### **FASE 8: Detalle de Publicación** 📄
*Tiempo estimado: 3-4 días*

#### Objetivo
Pantalla completa de detalle de post con comentarios y respuestas anidadas.

#### Orden de Implementación

1. **PostDetailViewModel.kt**
   - Estados: `PostDetailUiState`
     - `post: Post?`
     - `comments: List<Comment>`
     - `isLoading: Boolean`
     - `error: String?`
     - `commentText: String`
     - `replyingTo: Comment?`
   - Funciones:
     - `loadPost(postId: Long)`
     - `loadComments()`
     - `likePost()`
     - `favoritePost()`
     - `createComment(text: String)`
     - `replyToComment(comment: Comment, text: String)`
     - `likeComment(commentId: Long)`
     - `deletePost()`
     - `deleteComment(commentId: Long)`

2. **PostDetailScreen.kt**
   - Top App Bar con botón de back
   - Card del post completo
   - Sección de comentarios
   - Input de comentario en la parte inferior
   - Funcionalidad de scroll
   - Botones de acción (editar, eliminar) si es propio

3. **CommentItem.kt** (Componente)
   - Avatar y nombre del usuario
   - Texto del comentario
   - Botón de like y contador
   - Botón de responder
   - Fecha
   - Mostrar respuestas anidadas (recursivo)

4. **CommentInput.kt** (Componente)
   - Campo de texto
   - Botón de enviar
   - Modo "responder a comentario"
   - Placeholder dinámico

5. **Funcionalidad de respuestas anidadas**
   - Mostrar comentarios en forma de árbol
   - Indentación visual para respuestas
   - Límite de profundidad (opcional)

#### ✅ Checklist
- [ ] PostDetailScreen mostrando post completo
- [ ] Lista de comentarios funcionando
- [ ] Crear comentario funcionando
- [ ] Respuestas anidadas mostrándose correctamente
- [ ] Like en comentarios funcionando
- [ ] Eliminar comentario funcionando
- [ ] Modo offline funcionando

---

### **FASE 9: Crear/Editar Publicación** ✏️
*Tiempo estimado: 3-4 días*

#### Objetivo
Permitir crear y editar publicaciones con múltiples imágenes.

#### Orden de Implementación

1. **PostCreateViewModel.kt**
   - Estados: `PostCreateUiState`
     - `title: String`
     - `description: String`
     - `images: List<String>` (URIs locales)
     - `isLoading: Boolean`
     - `error: String?`
   - Funciones:
     - `addImage(uri: String)`
     - `removeImage(uri: String)`
     - `createPost()`
     - `updatePost(postId: Long)`
     - Validaciones

2. **PostCreateScreen.kt**
   - Top App Bar con botón cancelar y guardar
   - Campo de título
   - Campo de descripción (multilínea)
   - Sección de imágenes:
     - Botón para agregar imagen (cámara o galería)
     - Grid de imágenes seleccionadas
     - Botón para eliminar imagen
   - Indicador de carga al guardar
   - Manejo de errores

3. **ImagePicker.kt** (Utilidad)
   - Funciones para:
     - Abrir galería
     - Abrir cámara
     - Manejar permisos

4. **ImagePreview.kt** (Componente)
   - Mostrar imagen con opción de eliminar
   - Zoom (opcional)

5. **PostEditScreen.kt**
   - Similar a Create pero precargando datos
   - O reutilizar PostCreateScreen con modo "edit"

6. **Subida de imágenes**
   - Convertir URI a File
   - Subir a servidor (si aplica)
   - O guardar URIs locales para sincronización posterior

#### ✅ Checklist
- [ ] Crear post funcionando
- [ ] Agregar múltiples imágenes
- [ ] Editar post funcionando
- [ ] Modo offline guardando correctamente
- [ ] Validaciones funcionando
- [ ] Permisos de cámara/galería manejados

---

### **FASE 10: Perfil de Usuario** 👤
*Tiempo estimado: 2-3 días*

#### Objetivo
Mostrar perfil de usuario y permitir edición.

#### Orden de Implementación

1. **ProfileViewModel.kt**
   - Estados: `ProfileUiState`
     - `user: User?`
     - `posts: List<Post>`
     - `isLoading: Boolean`
     - `error: String?`
   - Funciones:
     - `loadProfile(userId: Long)`
     - `loadUserPosts(userId: Long)`
     - `deletePost(postId: Long)`

2. **ProfileScreen.kt**
   - Header con avatar, nombre, alias
   - Botón de editar (si es propio perfil)
   - Segmented control: Posts / Favoritos
   - Lista de posts del usuario
   - Lista de favoritos (si aplica)
   - Botón de logout (si es propio perfil)

3. **ProfileEditViewModel.kt**
   - Estados similares a registro pero para edición
   - Funciones:
     - `updateProfile(user: User)`
     - `updateAvatar(uri: String)`
     - `changePassword(oldPassword: String, newPassword: String)`

4. **ProfileEditScreen.kt**
   - Formulario de edición
   - Cambiar avatar
   - Cambiar contraseña (opcional, pantalla separada)

#### ✅ Checklist
- [ ] ProfileScreen mostrando datos correctos
- [ ] Lista de posts del usuario
- [ ] Editar perfil funcionando
- [ ] Cambiar avatar funcionando
- [ ] Logout funcionando

---

### **FASE 11: Favoritos** ⭐
*Tiempo estimado: 2 días*

#### Objetivo
Pantalla dedicada a publicaciones favoritas con búsqueda y ordenamiento.

#### Orden de Implementación

1. **FavoritesViewModel.kt**
   - Estados: `FavoritesUiState`
     - `posts: List<Post>`
     - `filteredPosts: List<Post>`
     - `searchQuery: String`
     - `sortOrder: SortOrder` (Por título, fecha, usuario)
     - `isLoading: Boolean`
   - Funciones:
     - `loadFavorites()`
     - `search(query: String)`
     - `sortBy(order: SortOrder)`
     - `removeFavorite(postId: Long)`

2. **FavoritesScreen.kt**
   - Top App Bar con título
   - Barra de búsqueda
   - Filtros/ordenamiento (dropdown o chips)
   - Lista de posts favoritos (reutilizar PostCard)
   - Estado vacío cuando no hay favoritos

3. **SortOrder.kt** (Enum o Sealed class)
   - Por título (A-Z)
   - Por fecha (más reciente primero)
   - Por usuario

#### ✅ Checklist
- [ ] Favoritos mostrándose correctamente
- [ ] Búsqueda funcionando
- [ ] Ordenamiento funcionando
- [ ] Remover de favoritos funcionando

---

### **FASE 12: Sincronización Offline** 🔄
*Tiempo estimado: 3-4 días*

#### Objetivo
Implementar sincronización automática cuando hay conexión.

#### Orden de Implementación

1. **SyncWorker.kt** (WorkManager)
   - Periódico: Sincronizar cada X minutos si hay conexión
   - Inmediato: Sincronizar al detectar conexión
   - Sincronizar:
     - Posts no sincronizados
     - Comentarios no sincronizados
     - Actualizar posts locales con datos del servidor

2. **NetworkMonitor integrado**
   - Observar cambios de conexión
   - Disparar sincronización automáticamente

3. **Lógica de sincronización en Repositorios**
   - `syncPosts()`: Enviar posts con `synced = false`
   - `syncComments()`: Enviar comentarios con `synced = false`
   - Actualizar posts locales con datos del servidor
   - Manejar conflictos (estrategia: server wins o last write wins)

4. **Estado de sincronización en UI**
   - Indicador de "sincronizando..."
   - Indicador de "offline"
   - Banner cuando se completa sincronización

5. **Pantalla de Sincronización** (opcional)
   - Mostrar progreso de sincronización
   - Lista de elementos pendientes

#### ✅ Checklist
- [ ] SyncWorker configurado
- [ ] Sincronización automática al reconectarse
- [ ] Sincronización periódica funcionando
- [ ] Manejo de conflictos implementado
- [ ] UI mostrando estado de sincronización

---

### **FASE 13: Manejo de Errores y Estados** ⚠️
*Tiempo estimado: 2 días*

#### Objetivo
Mejorar manejo de errores y estados en toda la app.

#### Implementación

1. **ErrorDialog.kt** (Componente)
   - Mostrar errores de manera consistente
   - Botones de acción según el error

2. **ErrorHandler.kt** (Utilidad)
   - Clasificar errores (red, servidor, validación, desconocido)
   - Mensajes de error amigables

3. **Estados vacíos**
   - EmptyState.kt componente
   - Mostrar en Feed, Favorites, Comments cuando no hay datos

4. **Loading states**
   - LoadingIndicator.kt mejorado
   - Skeleton loaders (opcional)

5. **Toast/Snackbar manager**
   - Mostrar mensajes de éxito/error consistentemente

#### ✅ Checklist
- [ ] ErrorDialog en todas las pantallas necesarias
- [ ] Estados vacíos mostrándose
- [ ] Mensajes de error amigables
- [ ] Feedback visual consistente

---

### **FASE 14: Ajustes Finales y Pulimiento** ✨
*Tiempo estimado: 2-3 días*

#### Objetivo
Ajustes finales, optimizaciones y mejoras de UX.

#### Tareas

1. **Optimizaciones**
   - Lazy loading de imágenes
   - Paginación en Feed (opcional)
   - Caché de imágenes con Coil
   - Optimización de queries de Room

2. **Accesibilidad**
   - Content descriptions
   - Navegación por teclado
   - Tamaños de texto configurables

3. **Testing básico**
   - Pruebas de flujos principales
   - Pruebas offline
   - Pruebas de sincronización

4. **Documentación**
   - Comentarios en código complejo
   - README actualizado

5. **Pulimiento UI**
   - Animaciones suaves
   - Transiciones entre pantallas
   - Feedback táctil (haptics)

#### ✅ Checklist
- [ ] Optimizaciones aplicadas
- [ ] Accesibilidad básica implementada
- [ ] Testing manual realizado
- [ ] UI pulida y consistente

---

## 📊 Resumen de Fases

| Fase | Descripción | Prioridad | Tiempo |
|------|-------------|-----------|--------|
| 0 | Utilidades Base | 🔴 Alta | 1-2 días |
| 1 | Base de Datos | 🔴 Alta | 2-3 días |
| 2 | Modelos Dominio | 🔴 Alta | 1 día |
| 3 | API y DTOs | 🔴 Alta | 2-3 días |
| 4 | Repositorios | 🔴 Alta | 2-3 días |
| 5 | Autenticación UI | 🔴 Alta | 3-4 días |
| 6 | Navegación | 🔴 Alta | 1-2 días |
| 7 | Feed | 🟡 Media | 3-4 días |
| 8 | Detalle Post | 🟡 Media | 3-4 días |
| 9 | Crear/Editar Post | 🟡 Media | 3-4 días |
| 10 | Perfil | 🟡 Media | 2-3 días |
| 11 | Favoritos | 🟢 Baja | 2 días |
| 12 | Sincronización | 🔴 Alta | 3-4 días |
| 13 | Errores/Estados | 🟡 Media | 2 días |
| 14 | Ajustes Finales | 🟢 Baja | 2-3 días |

**Tiempo Total Estimado: 30-45 días** (dependiendo del equipo y experiencia)

---

## 🎯 Orden Recomendado de Desarrollo

### Sprint 1 (Fundación) - Semanas 1-2
1. Fase 0: Utilidades Base
2. Fase 1: Base de Datos
3. Fase 2: Modelos Dominio
4. Fase 3: API y DTOs
5. Fase 4: Repositorios

### Sprint 2 (Autenticación y Navegación) - Semana 3
6. Fase 5: Autenticación UI
7. Fase 6: Navegación

### Sprint 3 (Funcionalidad Core) - Semanas 4-5
8. Fase 7: Feed
9. Fase 8: Detalle Post
10. Fase 9: Crear/Editar Post

### Sprint 4 (Perfiles y Favoritos) - Semana 6
11. Fase 10: Perfil
12. Fase 11: Favoritos

### Sprint 5 (Offline y Pulimiento) - Semanas 7-8
13. Fase 12: Sincronización Offline
14. Fase 13: Manejo de Errores
15. Fase 14: Ajustes Finales

---

## 📝 Notas Importantes

### Prioridades
1. **Debe funcionar offline**: Priorizar guardado local sobre llamadas API
2. **Sincronización automática**: WorkManager es crítico para la experiencia
3. **Validaciones robustas**: Especialmente en autenticación y creación de posts
4. **Feedback visual**: Siempre mostrar estados (loading, error, éxito)

### Buenas Prácticas
- **Offline-first**: Siempre guardar en local primero
- **Flows para reactividad**: Usar StateFlow/Flow para UI reactiva
- **Recursos reutilizables**: Crear componentes reutilizables desde el inicio
- **Manejo de errores consistente**: Usar Resource pattern en todos lados

### Integración con Backend
- Asegurar que todos los endpoints estén documentados
- Probar cada endpoint antes de implementar
- Manejar códigos de error HTTP apropiadamente
- Considerar rate limiting y paginación

---

## 🚀 Siguiente Paso

**Empezar con FASE 0: Configuración Base y Utilidades**

¿Listo para comenzar? ¡Vamos a implementar las utilidades base! 🎉


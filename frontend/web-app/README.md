# SupplyBoost Web Application

Modern e-commerce frontend built with Vue 3, TypeScript, and Vite.

## Features

- **User Authentication**: Login and registration with JWT tokens
- **Product Catalog**: Browse and search products
- **Shopping Cart**: Add, update, and remove items
- **Checkout**: Complete order placement with shipping details
- **Order History**: View past orders and track status
- **Responsive Design**: Mobile-friendly UI
- **Type Safety**: Full TypeScript support
- **State Management**: Pinia for reactive state
- **Routing**: Vue Router for navigation

## Technology Stack

- **Framework**: Vue 3 with Composition API
- **Language**: TypeScript
- **Build Tool**: Vite
- **State Management**: Pinia
- **Routing**: Vue Router 4
- **HTTP Client**: Axios
- **Styling**: Scoped CSS

## Prerequisites

- Node.js 18+ and npm

## Development

### Install Dependencies

```bash
npm install
```

### Run Development Server

```bash
npm run dev
```

The application will be available at http://localhost:5173

### Build for Production

```bash
npm run build
```

### Preview Production Build

```bash
npm run preview
```

## Environment Variables

Create a `.env.development` file for local development:

```
VITE_API_BASE_URL=http://localhost:8080
```

For production, create `.env.production`:

```
VITE_API_BASE_URL=https://api.supplyboost.com
```

## Project Structure

```
src/
├── api/            # API client and service modules
├── components/     # Reusable Vue components
├── router/         # Vue Router configuration
├── stores/         # Pinia stores
├── types/          # TypeScript type definitions
├── views/          # Page components
├── App.vue         # Root component
└── main.ts         # Application entry point
```

## License

Apache License 2.0

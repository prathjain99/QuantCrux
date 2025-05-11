

// /** @type {import('tailwindcss').Config} */
// export default {
//   content: [
//     "./index.html",
//     "./src/**/*.{js,ts,jsx,tsx}",
//   ],
//   theme: {
//     extend: {
//       colors: {
//         primary: "#3B82F6", // Tailwind's blue-500
//         background: "#0F172A", // navy/dark background
//         card: "#1E293B", // dark card
//         text: "#F1F5F9", // light text
//       },
//       fontFamily: {
//       sans: ['Inter', 'sans-serif'],
//     },
//     },
//   },
//   plugins: [],
// }

// tailwind.config.js
/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        background: "#0F172A", // custom dark background
        text: "#F1F5F9", // custom text color
        primary: "#3B82F6", // primary color
        card: "#1E293B", // card background color
      },
    },
  },
  plugins: [],
};

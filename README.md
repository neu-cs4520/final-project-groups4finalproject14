# Movie Review App

## Summary

The Movie Review App is designed to provide users with a platform to discover, review, and enjoy movies. Users can search for new movies and read and write reviews. The app will offer personalized recommendations, trending movie lists, and the ability to save favorite movies for easy access.

## Features

1.  **User Registration and Authentication**: Secure sign-up and login functionality using Firebase Authentication.
2.  **Movie Search and Discovery**: Search for movies by title, genre, release year, and rating with advanced search filters for a robust and user-friendly experience.
3.  **Movie Details and Reviews**: View detailed information about movies, including trailers, cast, movie summary, and reviews.
4.  **Write and Edit Reviews**: Users can write, edit, and delete their reviews.
5.  **Favorites List**: Add and remove movies to a favorites list for quick access.
6.  **Notifications**: Receive notifications for new reviews and personalized movie recommendations.
7.  **Social Sharing**: Share movie reviews via email and social media platforms such as X/Twitter and Reddit.
8.  **User Profiles**: Create and customize user profiles, including a list of reviewed movies, favorite genres, favorite movies, and personalized movie recommendations.

## Initial Wireframe

- **Login Screen**: Email, Password, Login Button, and Register Button.
- **Home Screen**: List of trending, now playing, top rated, and upcoming movies, and Navigation Menu.
- **Movie Search Screen**: Input field for search queries and search results list with advanced filters section.
- **Movie Details Screen**: Movie information, trailer, ratings, user reviews, and buttons to add review or favorite.
- **Review Writing Screen**: Input fields for writing reviews and Save and Delete buttons.
- **User Profile Screen**: Profile details, list of reviewed, favorited, and recommended movies, and add or remove favorite genres.

## Concepts from Course Syllabus

### Activities:

- **MainActivity**: For navigation.
- **LoginRegisterActivity**: For user login and registration.
- **MovieDetailsActivity**: For viewing movie details.
- **ReviewActivity**: For writing reviews.

### Fragments:

- **HomeFragment**: For the home screen.
- **SearchFragment**: For movie search.
- **ProfileFragment**: For user profiles.

### Storage (Firebase):

- Use **Firebase Firestore** for storing reviews, user favorites, and user recommendations.
- Use **Firebase Authentication** for user authentication.

### REST APIs:

- Integrate with **The Movie Database (TMDb) API** to fetch movie information and trailers.

### Background Services:

- Implement a background service to send notifications for new reviews and recommendations.

### UI Design:

- Focus on design principles for a user-friendly interface, responsive layouts, and smooth navigation.

## Testing

`java // API key for accessing the TMDb API String apiKey = "f395e60703b619ebfdb8421e6a5d94bd"; `

## Resources

- For more information, visit [TMDb API Documentation](https://developers.themoviedb.org/3/getting-started/introduction)

Team Members

---

-   **Group**: Groups4FinalProject14

-   **Member**: Noah Haniph (individual)

# ORJPS (Online Robotic Java Proxy Scanner)

## Overview
ORJPS is a Java-based web crawling and proxy scanning application designed for online research. It provides a graphical user interface for configuring and managing web crawling operations, discovering URLs, and collecting proxy information from web pages.

Version: 2.0
Author: Nackloose

## Features
- Multi-threaded web crawling with configurable thread count
- URL discovery and management
- Proxy detection and collection
- Real-time statistics and performance monitoring
- Pause/resume/stop functionality for crawling operations
- Duplicate URL and data detection

## Architecture
The application is built using Java Swing for the UI and follows an object-oriented design approach:

### Key Components
- **Spyder**: Main application entry point
- **Gui**: Graphical user interface for interaction
- **Scraper**: Core component for web crawling functionality
- **ScrapingThread**: Handles individual web page scraping in separate threads
- **DataList**: Manages the collection of unique data from web pages

### Threading Model
ORJPS uses a multi-threaded approach to perform web crawling operations:
- A ThreadController manages and monitors all scraping threads
- ScrapingThread instances perform the actual work of loading and parsing web pages
- Thread count can be adjusted through the UI for optimal performance

## Building and Running
ORJPS is built using NetBeans IDE and Ant build system.

### Requirements
- Java Development Kit (JDK) 8 or higher
- NetBeans IDE (recommended for development)

### Build Instructions
1. Open the project in NetBeans IDE
2. Clean and build the project using the NetBeans build command or:
   ```
   ant clean
   ant build
   ```

### Running the Application
1. Run the project from NetBeans IDE, or
2. Execute the JAR file from the command line:
   ```
   java -jar dist/ORJPS.jar
   ```

## Usage
1. Start the application
2. Enter URLs to scrape in the input field
3. Configure the desired number of threads using the spinner control
4. Click "Start" to begin crawling
5. Monitor progress in the UI, including:
   - URLs discovered
   - Proxies found
   - Network statistics
   - Crawling performance metrics
6. Use "Pause" to temporarily stop crawling operations
7. Use "Stop" to completely halt the crawler

## User Interface
The application uses a tabbed interface with the following main components:
- URL management panel
- Configuration options
- Status and statistics display
- Results table showing discovered URLs and proxies

## Dependencies
- Java Swing (UI components)
- Custom web browser implementation (com.congeriem.web.browser)

## License
This software is licensed under the MIT License.
See [LICENSE](LICENSE)

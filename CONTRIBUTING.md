# Contributing to JournalForge

Thank you for your interest in contributing to JournalForge! This document provides guidelines and information for contributors.

## Table of Contents
- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Process](#development-process)
- [Coding Standards](#coding-standards)
- [Submitting Changes](#submitting-changes)
- [Feature Requests](#feature-requests)
- [Bug Reports](#bug-reports)

## Code of Conduct

### Our Pledge
- Be respectful and inclusive
- Focus on constructive feedback
- Prioritize user privacy and data security
- Maintain professional communication

### Unacceptable Behavior
- Harassment or discrimination
- Sharing private information
- Trolling or intentionally disruptive behavior
- Any conduct inappropriate in a professional setting

## Getting Started

### Prerequisites
1. Read [GETTING_STARTED.md](GETTING_STARTED.md) for setup
2. Review [README.md](README.md) for architecture and features
3. Familiarize yourself with Kotlin and Android development

### Setting Up Development Environment

```bash
# Clone repository
git clone https://github.com/gherrick0918/JournalForge.git
cd JournalForge/android-app

# Create a new branch
git checkout -b feature/your-feature-name

# Open in Android Studio
# File → Open → Select android-app directory

# Build the project
./gradlew assembleDebug
```

## Development Process

### Branch Strategy

- `main` - Stable release branch
- `develop` - Development branch
- `feature/*` - New features
- `bugfix/*` - Bug fixes
- `hotfix/*` - Critical production fixes

### Workflow

1. **Pick an Issue**
   - Check open issues
   - Comment that you're working on it
   - Get assigned to the issue

2. **Create Branch**
   ```bash
   git checkout -b feature/issue-number-description
   ```

3. **Develop**
   - Write code
   - Follow coding standards
   - Add/update tests
   - Update documentation

4. **Test**
   - Run unit tests: `./gradlew test`
   - Test on Android emulator or device
   - Check for regressions

5. **Commit**
   ```bash
   git add .
   git commit -m "feat: add new feature description"
   ```

6. **Push**
   ```bash
   git push origin feature/your-branch-name
   ```

7. **Pull Request**
   - Open PR to `main` branch
   - Fill out PR template
   - Request review

## Coding Standards

### Kotlin Style Guide

#### Naming Conventions

```kotlin
// Classes, Interfaces: PascalCase
class JournalEntry { }
interface AIService { }

// Functions, Properties: camelCase
fun saveEntry() { }
val title: String = ""

// Private properties: camelCase (no underscore)
private val entryId: String = ""

// Constants: UPPER_SNAKE_CASE
private const val MAX_LENGTH = 1000

// Package names: lowercase
package com.journalforge.app.services
```

#### Code Organization

```kotlin
// 1. Package declaration
package com.journalforge.app.services

// 2. Imports (sorted)
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.journalforge.app.models.JournalEntry

// 3. Class with proper ordering
class ExampleService(private val context: Context) {
    
    // Companion object (if needed)
    companion object {
        private const val TAG = "ExampleService"
    }
    
    // Properties
    private val firebaseAuth = FirebaseAuth.getInstance()
    
    // Init block
    init {
        // Initialization code
    }
    
    // Public methods
    fun publicMethod() { }
    
    // Private methods
    private fun privateMethod() { }
}
```

#### Best Practices

1. **Coroutines for Async**
   ```kotlin
   // Good
   suspend fun getEntry(id: String): JournalEntry {
       return withContext(Dispatchers.IO) {
           service.fetch(id)
       }
   }
   
   // Usage in Activity/Fragment
   lifecycleScope.launch {
       val entry = viewModel.getEntry(id)
   }
   ```

2. **Null Safety**
   ```kotlin
   // Good
   fun getTitle(entry: JournalEntry?): String {
       return entry?.title ?: "Untitled"
   }
   
   // Use safe calls
   val email = user?.email
   
   // Use non-null assertion only when certain
   val name = user!!.name  // Use sparingly
   ```

3. **Collections**
   ```kotlin
   // Good - Use Kotlin collection extensions
   val recent = entries
       .sortedByDescending { it.createdDate }
       .take(10)
   
   // Use immutable collections when possible
   val list: List<String> = listOf("a", "b", "c")
   ```

### XML Layout Style Guide

#### Structure

```xml
<!-- Good structure -->
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
    <!-- Content here -->
    
</androidx.constraintlayout.widget.ConstraintLayout>
```

#### Naming

```xml
<!-- Use snake_case for IDs -->
<EditText
    android:id="@+id/title_entry"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />

<!-- Use descriptive names -->
<Button
    android:id="@+id/btn_save"
    android:text="@string/btn_save" />
```

#### Bindings

```xaml
<!-- One-way binding (default) -->
<Label Text="{Binding Title}" />

<!-- Two-way binding for inputs -->
<Entry Text="{Binding Title, Mode=TwoWay}" />

<!-- With value converter -->
<Label IsVisible="{Binding HasContent, Converter={StaticResource BoolConverter}}" />
```

### Comments

```csharp
// Use comments sparingly - code should be self-documenting

// Good: Explains WHY
// Using exponential backoff to prevent API rate limiting
await Task.Delay(delay * 2);

// Bad: Explains WHAT (obvious from code)
// Increment counter by 1
counter++;

// Use XML comments for public APIs
/// <summary>
/// Saves a journal entry to storage.
/// </summary>
/// <param name="entry">The entry to save.</param>
/// <returns>True if successful, false otherwise.</returns>
public async Task<bool> SaveEntryAsync(JournalEntry entry)
{
    // Implementation
}
```

## Testing

### Unit Tests

```csharp
[Fact]
public async Task SaveEntry_ValidEntry_ReturnsTrue()
{
    // Arrange
    var service = new JournalEntryService();
    var entry = new JournalEntry 
    { 
        Title = "Test",
        Content = "Content" 
    };
    
    // Act
    var result = await service.SaveEntryAsync(entry);
    
    // Assert
    Assert.True(result);
}
```

### Test Coverage

- Aim for 80%+ code coverage
- Focus on business logic
- Test edge cases and error handling

## Submitting Changes

### Commit Messages

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
feat: add voice recording feature
fix: correct date display in time capsules
docs: update README with new features
style: format code according to guidelines
refactor: simplify AI service implementation
test: add unit tests for journal service
chore: update dependencies
```

### Pull Request Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
- [ ] Unit tests pass
- [ ] Manual testing completed
- [ ] Tested on Android
- [ ] Tested on iOS
- [ ] Tested on Windows

## Checklist
- [ ] Code follows style guidelines
- [ ] Self-review completed
- [ ] Comments added for complex code
- [ ] Documentation updated
- [ ] No new warnings generated

## Screenshots (if applicable)
```

### Review Process

1. Automated checks run (build, tests)
2. Code review by maintainer
3. Address feedback
4. Approval and merge

## Feature Requests

### Submitting Ideas

Use GitHub Issues with template:

```markdown
**Feature Description**
Clear description of the feature

**Use Case**
Why is this feature needed?

**Proposed Solution**
How should it work?

**Alternatives Considered**
Other approaches you thought about

**Priority**
- [ ] Critical
- [ ] High
- [ ] Medium
- [ ] Low
```

## Bug Reports

### Reporting Bugs

Use GitHub Issues with template:

```markdown
**Bug Description**
Clear description of the bug

**Steps to Reproduce**
1. Go to...
2. Click on...
3. See error

**Expected Behavior**
What should happen

**Actual Behavior**
What actually happens

**Screenshots**
If applicable

**Environment**
- OS: [e.g., Android 14]
- Device: [e.g., Pixel 7]
- App Version: [e.g., 1.0.0]

**Additional Context**
Any other relevant information
```

## Project Structure

### Adding New Features

When adding a new feature:

1. **Model** (if needed)
   - Add to `models/` directory in android-app
   - Create Kotlin data classes

2. **Service** (if needed)
   - Create service class in `services/`
   - Implement business logic
   - Use dependency injection if needed

3. **Activity/Fragment**
   - Add to `ui/` directory
   - Create layout XML in `res/layout/`
   - Implement view logic

4. **Navigation**
   - Update navigation graph if using Navigation Component
   - Add menu items if needed

5. **Tests**
   - Add unit tests for services
   - Add instrumentation tests for UI
   - Aim for good test coverage

## Resources

### Documentation
- [Android Developer Docs](https://developer.android.com/docs)
- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Firebase for Android](https://firebase.google.com/docs/android/setup)

### Community
- GitHub Issues for questions
- GitHub Discussions for ideas
- Pull Requests for contributions

## Questions?

- Check existing documentation
- Search closed issues
- Open a new issue with "question" label
- Reach out to maintainers

---

Thank you for contributing to JournalForge! 🎉

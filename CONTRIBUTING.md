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
2. Review [DESIGN.md](DESIGN.md) for architecture
3. Check [FEATURES.md](FEATURES.md) for feature details

### Setting Up Development Environment

```bash
# Clone repository
git clone https://github.com/gherrick0918/JournalForge.git
cd JournalForge

# Create a new branch
git checkout -b feature/your-feature-name

# Install dependencies
dotnet restore

# Build and run
dotnet build
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
   - Run unit tests
   - Test on multiple platforms
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
   - Open PR to `develop` branch
   - Fill out PR template
   - Request review

## Coding Standards

### C# Style Guide

#### Naming Conventions

```csharp
// Classes, Methods, Properties: PascalCase
public class JournalEntry { }
public void SaveEntry() { }
public string Title { get; set; }

// Private fields: _camelCase with underscore
private string _entryId;

// Local variables, parameters: camelCase
public void Method(string parameterName)
{
    var localVariable = "value";
}

// Constants: UPPER_CASE
private const string MAX_LENGTH = 1000;

// Interfaces: I + PascalCase
public interface IAIService { }
```

#### Code Organization

```csharp
// 1. Usings (sorted alphabetically)
using System;
using System.Collections.Generic;
using JournalForge.Models;

// 2. Namespace
namespace JournalForge.Services;

// 3. Class with proper ordering
public class ExampleService
{
    // Private fields
    private readonly IAIService _aiService;
    
    // Constructor
    public ExampleService(IAIService aiService)
    {
        _aiService = aiService;
    }
    
    // Public properties
    public string Name { get; set; }
    
    // Public methods
    public async Task DoSomethingAsync()
    {
        // Implementation
    }
    
    // Private methods
    private void HelperMethod()
    {
        // Implementation
    }
}
```

#### Best Practices

1. **Async/Await**
   ```csharp
   // Good
   public async Task<JournalEntry> GetEntryAsync(string id)
   {
       return await _service.FetchAsync(id);
   }
   
   // Bad
   public JournalEntry GetEntry(string id)
   {
       return _service.FetchAsync(id).Result; // Blocks thread
   }
   ```

2. **Null Safety**
   ```csharp
   // Good
   public string GetTitle(JournalEntry? entry)
   {
       return entry?.Title ?? "Untitled";
   }
   
   // Bad
   public string GetTitle(JournalEntry entry)
   {
       return entry.Title; // Might throw NullReferenceException
   }
   ```

3. **LINQ Usage**
   ```csharp
   // Good
   var recent = entries
       .OrderByDescending(e => e.CreatedDate)
       .Take(10)
       .ToList();
   
   // Bad
   var recent = new List<JournalEntry>();
   foreach (var entry in entries)
   {
       if (recent.Count < 10)
           recent.Add(entry);
   }
   ```

### XAML Style Guide

#### Structure

```xaml
<!-- Good structure -->
<ContentPage xmlns="http://schemas.microsoft.com/dotnet/2021/maui"
             xmlns:x="http://schemas.microsoft.com/winfx/2009/xaml"
             x:Class="JournalForge.Pages.ExamplePage"
             Title="Example">
    
    <!-- Content here -->
    
</ContentPage>
```

#### Naming

```xaml
<!-- Use x:Name for elements you need to reference -->
<Entry x:Name="TitleEntry" />

<!-- Use descriptive names -->
<Button x:Name="SaveButton" Text="Save" />
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
   - Add to `Models/` directory
   - Include properties and basic logic

2. **Service** (if needed)
   - Create interface in `Services/`
   - Implement interface
   - Register in `MauiProgram.cs`

3. **ViewModel**
   - Add to `ViewModels/`
   - Inherit from `BaseViewModel`
   - Implement commands and properties

4. **View**
   - Add XAML to `Pages/`
   - Create code-behind
   - Wire up to ViewModel

5. **Navigation**
   - Register route in `AppShell.xaml.cs`
   - Add to Shell if needed

6. **Tests**
   - Add unit tests for service
   - Add unit tests for ViewModel
   - Add integration tests if needed

## Resources

### Documentation
- [.NET MAUI Docs](https://docs.microsoft.com/dotnet/maui/)
- [MVVM Pattern](https://docs.microsoft.com/xamarin/xamarin-forms/enterprise-application-patterns/mvvm)
- [C# Coding Conventions](https://docs.microsoft.com/dotnet/csharp/fundamentals/coding-style/coding-conventions)

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

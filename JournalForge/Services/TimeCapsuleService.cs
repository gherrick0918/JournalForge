using JournalForge.Models;

namespace JournalForge.Services;

public interface ITimeCapsuleService
{
    Task<List<TimeCapsule>> GetAllCapsulesAsync();
    Task<TimeCapsule?> GetCapsuleByIdAsync(string id);
    Task<bool> SealCapsuleAsync(TimeCapsule capsule);
    Task<bool> UnsealCapsuleAsync(string id);
    Task<List<TimeCapsule>> GetReadyToUnsealCapsulesAsync();
}

public class TimeCapsuleService : ITimeCapsuleService
{
    private readonly List<TimeCapsule> _capsules = new();

    public Task<List<TimeCapsule>> GetAllCapsulesAsync()
    {
        return Task.FromResult(_capsules.OrderByDescending(c => c.SealedDate).ToList());
    }

    public Task<TimeCapsule?> GetCapsuleByIdAsync(string id)
    {
        var capsule = _capsules.FirstOrDefault(c => c.Id == id);
        return Task.FromResult(capsule);
    }

    public Task<bool> SealCapsuleAsync(TimeCapsule capsule)
    {
        var existing = _capsules.FirstOrDefault(c => c.Id == capsule.Id);
        if (existing != null)
        {
            _capsules.Remove(existing);
        }
        
        _capsules.Add(capsule);
        return Task.FromResult(true);
    }

    public Task<bool> UnsealCapsuleAsync(string id)
    {
        var capsule = _capsules.FirstOrDefault(c => c.Id == id);
        if (capsule != null)
        {
            capsule.IsUnsealed = true;
            return Task.FromResult(true);
        }
        return Task.FromResult(false);
    }

    public Task<List<TimeCapsule>> GetReadyToUnsealCapsulesAsync()
    {
        var ready = _capsules
            .Where(c => !c.IsUnsealed && c.UnsealDate <= DateTime.Now)
            .OrderBy(c => c.UnsealDate)
            .ToList();
        
        return Task.FromResult(ready);
    }
}

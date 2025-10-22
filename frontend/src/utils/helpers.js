export const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleDateString('ru-RU', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });
};

export const getStatusColor = (status) => {
  const colors = {
    PENDING: '#F59E0B',
    APPROVED: '#10B981',
    REJECTED: '#EF4444',
    AVAILABLE: '#06B6D4',
    EXCHANGED: '#6B7280'
  };
  return colors[status] || '#6B7280';
};

export const validateEmail = (email) => {
  const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return re.test(email);
};

export const validateDisplayName = (name) => {
  return name.length >= 3 && name.length <= 120;
};

import React, { useState, useCallback } from 'react';
import { TextField, InputAdornment, IconButton } from '@mui/material';
import { SearchOutlined, ClearOutlined } from '@mui/icons-material';

/**
 * Debounced search bar component.
 *
 * Usage:
 *   <SearchBar
 *     placeholder="Search apartments…"
 *     onSearch={(value) => setKeyword(value)}
 *     debounceMs={400}
 *   />
 */
function SearchBar({ placeholder = 'Search…', onSearch, debounceMs = 400, fullWidth = false, sx }) {
  const [value, setValue] = useState('');
  const [timer, setTimer] = useState(null);

  const handleChange = useCallback((e) => {
    const val = e.target.value;
    setValue(val);
    if (timer) clearTimeout(timer);
    const t = setTimeout(() => onSearch(val), debounceMs);
    setTimer(t);
  }, [timer, debounceMs, onSearch]);

  const handleClear = () => {
    setValue('');
    onSearch('');
  };

  return (
    <TextField
      value={value}
      onChange={handleChange}
      placeholder={placeholder}
      size="small"
      fullWidth={fullWidth}
      sx={{ minWidth: 240, ...sx }}
      InputProps={{
        startAdornment: (
          <InputAdornment position="start">
            <SearchOutlined fontSize="small" color="action" />
          </InputAdornment>
        ),
        endAdornment: value ? (
          <InputAdornment position="end">
            <IconButton size="small" onClick={handleClear}>
              <ClearOutlined fontSize="small" />
            </IconButton>
          </InputAdornment>
        ) : null,
        sx: { borderRadius: '10px' },
      }}
    />
  );
}

export default SearchBar;

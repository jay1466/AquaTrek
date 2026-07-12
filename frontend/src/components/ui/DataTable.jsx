import React from 'react';
import {
  Table, TableBody, TableCell, TableContainer, TableHead,
  TableRow, TablePagination, TableSortLabel, Paper, Box,
  Typography, Skeleton, CircularProgress,
} from '@mui/material';
import { InboxOutlined } from '@mui/icons-material';

/**
 * Reusable data table with server-side pagination, sorting, loading skeletons,
 * and empty state.
 *
 * columns: [{ id, label, sortable, width, align, render }]
 * rows: array of data objects
 * pagination: { page, size, total, totalPages }
 * onPageChange: (newPage) => void
 * onSizeChange: (newSize) => void
 * onSort: (field, direction) => void
 */
function DataTable({
  columns = [],
  rows = [],
  loading = false,
  pagination,
  onPageChange,
  onSizeChange,
  sortField,
  sortDir = 'asc',
  onSort,
  emptyTitle = 'No records found',
  emptyMessage = 'Try adjusting your search or filters.',
  rowKey = 'id',
  stickyHeader = true,
  maxHeight,
}) {
  const skeletonRows = 6;

  const handleSort = (field) => {
    if (!onSort) return;
    const newDir = sortField === field && sortDir === 'asc' ? 'desc' : 'asc';
    onSort(field, newDir);
  };

  return (
    <Paper
      elevation={0}
      sx={{
        border: '1px solid',
        borderColor: 'divider',
        borderRadius: '12px',
        overflow: 'hidden',
      }}
    >
      <TableContainer sx={maxHeight ? { maxHeight } : {}}>
        <Table stickyHeader={stickyHeader} size="medium">

          {/* Head */}
          <TableHead>
            <TableRow>
              {columns.map((col) => (
                <TableCell
                  key={col.id}
                  align={col.align || 'left'}
                  width={col.width}
                  sx={{ fontWeight: 700, whiteSpace: 'nowrap' }}
                >
                  {col.sortable ? (
                    <TableSortLabel
                      active={sortField === col.id}
                      direction={sortField === col.id ? sortDir : 'asc'}
                      onClick={() => handleSort(col.id)}
                    >
                      {col.label}
                    </TableSortLabel>
                  ) : col.label}
                </TableCell>
              ))}
            </TableRow>
          </TableHead>

          {/* Body */}
          <TableBody>
            {loading ? (
              Array.from({ length: skeletonRows }).map((_, i) => (
                <TableRow key={i}>
                  {columns.map((col) => (
                    <TableCell key={col.id}>
                      <Skeleton variant="text" width="80%" />
                    </TableCell>
                  ))}
                </TableRow>
              ))
            ) : rows.length === 0 ? (
              <TableRow>
                <TableCell colSpan={columns.length} sx={{ border: 0 }}>
                  <Box
                    sx={{
                      py: 8, textAlign: 'center',
                      display: 'flex', flexDirection: 'column',
                      alignItems: 'center', gap: 1,
                    }}
                  >
                    <InboxOutlined sx={{ fontSize: 48, color: 'text.disabled' }} />
                    <Typography variant="h6" color="text.secondary" fontWeight={600}>
                      {emptyTitle}
                    </Typography>
                    <Typography variant="body2" color="text.disabled">
                      {emptyMessage}
                    </Typography>
                  </Box>
                </TableCell>
              </TableRow>
            ) : (
              rows.map((row) => (
                <TableRow
                  key={row[rowKey]}
                  hover
                  sx={{ '&:last-child td': { border: 0 } }}
                >
                  {columns.map((col) => (
                    <TableCell key={col.id} align={col.align || 'left'}>
                      {col.render ? col.render(row[col.id], row) : row[col.id] ?? '—'}
                    </TableCell>
                  ))}
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Pagination */}
      {pagination && (
        <TablePagination
          component="div"
          count={pagination.total || 0}
          page={pagination.page || 0}
          rowsPerPage={pagination.size || 20}
          onPageChange={(_, p) => onPageChange(p)}
          onRowsPerPageChange={(e) => onSizeChange(parseInt(e.target.value, 10))}
          rowsPerPageOptions={[10, 20, 50, 100]}
          sx={{ borderTop: '1px solid', borderColor: 'divider' }}
        />
      )}
    </Paper>
  );
}

export default DataTable;

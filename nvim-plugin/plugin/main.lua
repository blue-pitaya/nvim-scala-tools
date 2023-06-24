print("Hello man!")

-- Get the buffer number of the current buffer


local print_bufname = function ()
  local bufnr = vim.api.nvim_get_current_buf()
  local name = vim.api.nvim_buf_get_name(bufnr)
  local x = vim.api.nvim
  print(name)
end

local print_cursors_pos = function ()
  local winnr = vim.api.nvim_get_current_win()
  local cursor_pos = vim.api.nvim_win_get_cursor(winnr)
  print(cursor_pos[1]..' '..cursor_pos[2])
end

--local print_line = function ()
--  print(bufnr())
--end

--function lolo()
--  local customSuggestions = {
--    "apple",
--    "banana",
--    "cherry"
--  }
--  
--  vim.fn.complete(1, customSuggestions)
--end

vim.keymap.set("n", "<leader>1", print_cursors_pos)

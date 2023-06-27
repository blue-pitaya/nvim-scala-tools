local input_pipe_path = "/tmp/nvim_scala_tools_pipe_in"
local output_pipe_path = "/tmp/nvim_scala_tools_pipe_out"

local function make_request(line, cursor_x_pos)
  return "get_actions_for_line\n"..line.."\n"..cursor_x_pos
end

local function send(req)
  local pipe = io.open(input_pipe_path, "w+")
  if pipe == nil then
    error("Error opening input pipe for write.")
  end
  pipe:write(req)
  pipe:close()
end

local function recv()
  local pipe = io.open(output_pipe_path, "r")
  if pipe == nil then
    error("Error opening output pipe for write.")
  end
  local resp = pipe:read()
  pipe:close()
  return resp
end

local function ask_for_actions()
  local winnr = vim.api.nvim_get_current_win()
  local cursor_pos =  vim.api.nvim_win_get_cursor(winnr)
  local current_line = vim.api.nvim_get_current_line()
  local req = make_request(current_line, cursor_pos[2])
  send(req)
  local resp = recv()
  print(vim.inspect(resp))
end


vim.keymap.set("n", "<leader>1", ask_for_actions)

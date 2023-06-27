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
  local resp = pipe:read("*a")
  pipe:close()
  return resp
end

local function get_lines(str)
  local lines = {}
  for line in str:gmatch('[^\n]+') do
    table.insert(lines, line)
  end
  return lines
end

local function pack_by_2(tab)
  local res = {}
  local fst = nil
  local snd = nil
  for _, v in ipairs(tab) do
    if fst == nil then
      fst = v
      goto continue
    end
    if snd == nil then
      snd = v
    end
    table.insert(res, {fst, snd})
    fst = nil
    snd = nil
    ::continue::
  end
  return res
end

local function raw_response_to_actions(resp)
  local lines = get_lines(resp)
  if lines[1] ~= 'ok' then
    error('!!!')
  end
  table.remove(lines, 1)
  local packed = pack_by_2(lines)
  local actions = {}
  for _, v in ipairs(packed) do
    table.insert(actions, {name = v[1], replaced_line = v[2]})
  end
  return actions
end

local function ask_for_actions()
  local winnr = vim.api.nvim_get_current_win()
  local cursor_pos =  vim.api.nvim_win_get_cursor(winnr)
  local current_line = vim.api.nvim_get_current_line()
  local req = make_request(current_line, cursor_pos[2])
  send(req)
  local resp = recv()
  local actions = raw_response_to_actions(resp)
  local options = {}
  for _, v in ipairs(actions) do
    table.insert(options, v)
  end
  vim.ui.select(options, {
      prompt = 'Select case class to autocomplete:',
      format_item = function(item)
        return item.name
      end,
  }, function(choice)
    vim.api.nvim_set_current_line(choice.replaced_line)
  end)
end


vim.keymap.set("n", "<leader>1", ask_for_actions)
